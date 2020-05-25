/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.broker.consumer.position;

import com.google.common.base.Preconditions;
import com.jd.laf.extension.ExtensionManager;
import org.apache.commons.lang3.StringUtils;
import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.consumer.ConsumeConfig;
import org.joyqueue.broker.consumer.model.ConsumePartition;
import org.joyqueue.broker.consumer.position.model.Position;
import org.joyqueue.domain.Consumer;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.TopicConfig;
import org.joyqueue.domain.TopicName;
import org.joyqueue.event.EventType;
import org.joyqueue.event.MetaEvent;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.exception.JoyQueueException;
import org.joyqueue.nsr.event.AddConsumerEvent;
import org.joyqueue.nsr.event.AddPartitionGroupEvent;
import org.joyqueue.nsr.event.RemoveConsumerEvent;
import org.joyqueue.nsr.event.RemovePartitionGroupEvent;
import org.joyqueue.nsr.event.UpdatePartitionGroupEvent;
import org.joyqueue.store.PartitionGroupStore;
import org.joyqueue.store.StoreService;
import org.joyqueue.toolkit.concurrent.EventListener;
import org.joyqueue.toolkit.concurrent.LoopThread;
import org.joyqueue.toolkit.concurrent.NamedThreadFactory;
import org.joyqueue.toolkit.service.Service;
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 消费位置管理
 * <p>
 * Created by chengzhiliang on 2018/8/23.
 */
public class PositionManager extends Service {

    private static Logger logger = LoggerFactory.getLogger(PositionManager.class);
    // 存储服务
    private StoreService storeService;
    // 集群管理
    private ClusterManager clusterManager;
    // 消费配置
    private ConsumeConfig config;
    // 消费位点快照存储服务
    private PositionStore<ConsumePartition, Position> positionStore;
    // 补偿消费位置线程（10分钟跑一次）
    private LoopThread checkSubscribeThread;
    // 最近应答时间跟踪器
    private Map<ConsumePartition, /* 最新应答时间 */ AtomicLong> lastAckTimeTrace = new ConcurrentHashMap<>();
    // 刷新线程
    private ExecutorService flushIndexThread;
    // 后刷新时间
    private AtomicLong lastFlushIndexTimestamp = new AtomicLong();

    public PositionManager(ClusterManager clusterManager, StoreService storeService, ConsumeConfig consumeConfig) {
        this.clusterManager = clusterManager;
        this.storeService = storeService;
        this.config = consumeConfig;

        Preconditions.checkArgument(this.config != null, "config can not be null");
    }

    @Override
    protected void validate() throws Exception {
        super.validate();
        Preconditions.checkArgument(clusterManager != null, "cluster manager can not be null");

        if (positionStore == null) {
            positionStore = ExtensionManager.getOrLoadExtension(PositionStore.class);
            if (positionStore instanceof LocalFileStore) {
                ((LocalFileStore) positionStore).setBasePath(config.getConsumePositionPath());
            }
        }
        flushIndexThread = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(10), new NamedThreadFactory("joyqueue-consume-flush-index-threads", true));
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        positionStore.start();

        this.checkSubscribeThread = LoopThread.builder()
                .sleepTime(1000 * 60 * 10, 1000 * 60 * 10)
                .name("Check-Subscribe-Thread")
                .onException(e -> logger.error(e.getMessage(), e))
                .doWork(this::compensationPosition)
                .build();

        this.checkSubscribeThread.start();

        clusterManager.addListener(new AddConsumeListener());
        clusterManager.addListener(new RemoveConsumeListener());
        clusterManager.addListener(new AddPartitionGroupListener());
        clusterManager.addListener(new RemovePartitionGroupListener());
        clusterManager.addListener(new UpdatePartitionGroupListener());

        logger.info("PositionManager is started.");
    }

    /**
     * 补偿消费位置
     * 迭代当前broker上全部的
     */
    private void compensationPosition() {
        Iterator<ConsumePartition> iterator = positionStore.iterator();
        while (iterator.hasNext()) {
            ConsumePartition next = iterator.next();
            TopicConfig topicConfig = clusterManager.getTopicConfig(TopicName.parse(next.getTopic()));
            if (topicConfig != null) {
                PartitionGroup partitionGroup = topicConfig.fetchPartitionGroupByPartition(next.getPartition());
                if (partitionGroup != null) {
                    next.setPartitionGroup(partitionGroup.getGroup());
                }
            }

            // 检查是否有订阅关系
            Consumer.ConsumerPolicy consumerPolicy = clusterManager.tryGetConsumerPolicy(TopicName.parse(next.getTopic()), next.getApp());
            if (consumerPolicy == null) {
                // 没有订阅关系，则删除消费位置
                iterator.remove();

                logger.info("Remove consume position by ConsumePosition:[{}]", next.toString());
            }
        }
    }

    @Override
    protected void doStop() {
        super.doStop();
        flushIndexThread.shutdownNow();
        positionStore.forceFlush();
        positionStore.stop();

        logger.info("PositionManager is stopped.");
    }

    /**
     * 获取最近一次应答消费位置跟踪器
     *
     * @return
     */
    public Map<ConsumePartition, AtomicLong> getLastAckTimeTrace() {
        return lastAckTimeTrace;
    }

    public Map<ConsumePartition, Position> getConsumePosition(TopicName topic, String app, int partitionGroup) {
        final List<String> appList;
        if (app == null) {
            appList = clusterManager.getLocalSubscribeAppByTopic(topic);
        } else {
            appList = Arrays.asList(app);
        }

        Map<ConsumePartition, Position> consumeInfo = new HashMap<>();

        if (appList != null && !appList.isEmpty()) {
            List<PartitionGroup> partitionGroupList = clusterManager.getLocalPartitionGroups(topic);
            for (PartitionGroup group : partitionGroupList) {
                if (group.getGroup() == partitionGroup) {
                    Set<Short> partitions = group.getPartitions();
                    partitions.stream().forEach(partition ->
                            appList.stream().forEach(element -> {
                                ConsumePartition consumePartition = new ConsumePartition(topic.getFullName(), element, partition);
                                consumePartition.setPartitionGroup(partitionGroup);
                                Position position = positionStore.get(consumePartition);
                                consumeInfo.put(consumePartition, position);
                            })
                    );
                    break;
                }
            }
        }

        return consumeInfo;
    }

    /**
     * 根据主题+分区分组获取消费位置信息
     *
     * @param topic
     * @param partitionGroup
     * @return
     */
    public Map<ConsumePartition, Position> getConsumePosition(TopicName topic, int partitionGroup) {
        return getConsumePosition(topic, null, partitionGroup);
    }

    /**
     * 消费位置复制，可覆盖
     *
     * @param consumePositions
     * @return
     */
    public boolean setConsumePosition(Map<ConsumePartition, Position> consumePositions) {
        try {
            if (consumePositions == null) {
                return false;
            }
            // 替换内存中的位置信息
            Set<Map.Entry<ConsumePartition, Position>> entries = consumePositions.entrySet();
            entries.stream().forEach(entry -> {
                ConsumePartition key = entry.getKey();
                Position val = entry.getValue();
                positionStore.put(key, val);
            });

            // 刷盘
            tryForceFlush();
        } catch (Exception ex) {
            logger.error("set consume position error.", ex);
            return false;
        }

        return true;
    }

    /**
     * 获取指定分区的应答消息序号
     *
     * @param topic     消费主题
     * @param app       消费应用
     * @param partition 消费分区
     * @return 指定分区已经消费到的消息序号
     */
    public long getLastMsgAckIndex(TopicName topic, String app, short partition) throws JoyQueueException {
        ConsumePartition consumePartition = new ConsumePartition(topic.getFullName(), app, partition);
        Position position = positionStore.get(consumePartition);
        // 消费位置对象为空时，无此位置信息抛出异常
        if (position == null) {
            throw new JoyQueueException(JoyQueueCode.CONSUME_POSITION_NULL, "topic=" + topic + ",app=" + app + ",partition=" + partition);
        }
        return position.getAckCurIndex();
    }

    /**
     * 更新指定分区的应答消息序号
     *
     * @param topic     消费主题
     * @param app       消费应用
     * @param partition 消费分区
     * @param index     起始消息序号
     * @return 是否更新成功
     */
    public boolean updateLastMsgAckIndex(TopicName topic, String app, short partition, long index) throws JoyQueueException {
        return updateLastMsgAckIndex(topic, app, partition, index, true);
    }

    public boolean updateLastMsgAckIndex(TopicName topic, String app, short partition, long index, boolean isUpdatePullIndex) throws JoyQueueException {
        logger.debug("Update last ack index, topic:{}, app:{}, partition:{}, index:{}", topic, app, partition, index);
        // 检查索引有效性
        checkIndex(topic, partition, index);
        // 标记最近一次更新应答位置时间
        markLastAckTime(topic, app, partition);

        ConsumePartition consumePartition = new ConsumePartition(topic.getFullName(), app, partition);
        Position position = positionStore.get(consumePartition);
        if (position != null) {
            position.setAckCurIndex(index);
            if (isUpdatePullIndex) {
                position.setPullCurIndex(-1);
            }
        } else {
            logger.error("Position is null, topic:{}, app:{}, partition:{}, index:{}", topic, app, partition, index);
            // 补偿逻辑：如果当前broker是指定partition对应partitionGroup的leader，则按照给定index初始化Position，否则不处理
            addAndUpdatePosition(topic, app, partition, index);
        }
        return true;
    }

    /**
     * 检查更新的位置是否有效
     *
     * @param topic
     * @param partition
     * @param index
     * @throws JoyQueueException
     */
    private void checkIndex(TopicName topic, short partition, long index) throws JoyQueueException {
        Integer partitionGroupId = clusterManager.getPartitionGroupId(topic, partition);
        if (partitionGroupId == null) {
            // 元数据获取不到partitionGroup
            throw new JoyQueueException(JoyQueueCode.CONSUME_POSITION_META_DATA_NULL, String.format("topic:[%s], partition:[%s], index:[%s]", topic, partition, index));
        }

        PartitionGroupStore store = storeService.getStore(topic.getFullName(), partitionGroupId);

        long leftIndex = store.getLeftIndex(partition);
        if (index < leftIndex && index != -1) {
            throw new JoyQueueException(JoyQueueCode.SE_INDEX_UNDERFLOW , "index less than leftIndex error.");
        }

        long rightIndex = store.getRightIndex(partition);
        if (index > rightIndex) {
            throw new JoyQueueException(JoyQueueCode.SE_INDEX_UNDERFLOW , "index more than rightIndex error.");
        }


    }

    /**
     * 标记最后一次应答时间
     * @param topic
     * @param app
     * @param partition
     */
    private void markLastAckTime(TopicName topic, String app, short partition) {
        ConsumePartition consumePartition = new ConsumePartition(topic.getFullName(), app, partition);
        AtomicLong lastAckTime = lastAckTimeTrace.get(consumePartition);
        if (lastAckTime == null) {
            lastAckTime = new AtomicLong(SystemClock.now());
            lastAckTimeTrace.put(consumePartition, lastAckTime);
        } else {
            lastAckTime.set(SystemClock.now());
        }
    }

    /**
     * 添加并更新消费位置
     *
     * @param topic
     * @param app
     * @param partition
     * @param index
     */
    private void addAndUpdatePosition(TopicName topic, String app, short partition, long index) throws JoyQueueException {
        logger.info("Try to init a position by topic:{}, app:{}, partition:{}, curIndex:{}", topic.getFullName(), app, partition, index);

        if (topic == null || app == null || app.isEmpty()) {
            return;
        }
        checkState();
        // 从元数据中获取分区分组，如何分区分组不为空，则添加该partition的位置，否则不添加
        PartitionGroup partitionGroup = clusterManager.getPartitionGroup(topic, partition);
        if(partitionGroup == null) {
            logger.error("Fail to add and update partition consume position by topic:[{}], app:[{}], partition:[{}], index:[{}]",
                    topic.getFullName(), app, partition, index);
            throw new JoyQueueException(JoyQueueCode.FW_PARTITION_BROKER_NOT_LEADER, "");
        }
        ConsumePartition consumePartition = new ConsumePartition(topic.getFullName(), app, partition);
        consumePartition.setPartitionGroup(partitionGroup.getGroup());

        long currentIndex = Math.max(index, 0);
        // 为新订阅的应用初始化消费位置对象
        Position position = new Position(currentIndex, currentIndex, currentIndex, currentIndex);
        positionStore.putIfAbsent(consumePartition, position);

        logger.info("Success to add and update partition consume position by topic:{}, app:{}, partition:{}, curIndex:{}", topic.getFullName(), app, partition, currentIndex);
        // 落盘
        positionStore.forceFlush();
    }

    /**
     * 更新指定分区的应答消息开始序号
     *
     * @param topic     消费主题
     * @param app       消费应用
     * @param partition 消费分区
     * @param index     起始消息序号
     * @return 是否更新成功
     */
    public boolean updateStartMsgAckIndex(TopicName topic, String app, short partition, long index) throws JoyQueueException {
        logger.debug("Update stater ack index, topic:{}, app:{}, partition:{}, index:{}", topic, app, partition, index);
        ConsumePartition consumePartition = new ConsumePartition(topic.getFullName(), app, partition);
        Position position = positionStore.get(consumePartition);
        if (position != null) {
            position.setAckStartIndex(index);
        } else {
            logger.error("Position is null, topic:{}, app:{}, partition:{}, index:{}", topic, app, partition, index);
            // 补偿逻辑：如果当前broker是指定partition对应partitionGroup的leader，则按照给定index初始化Position，否则不处理
            addAndUpdatePosition(topic, app, partition, index);
        }
        return true;
    }

    /**
     * 获取指定分区的拉取消息序号
     *
     * @param topic     消费主题
     * @param app       消费应用
     * @param partition 消费分区
     * @return 指定分区已经消费到的消息序号
     */
    public long getLastMsgPullIndex(TopicName topic, String app, short partition) throws JoyQueueException {
        ConsumePartition consumePartition = new ConsumePartition(topic.getFullName(), app, partition);
        Position position = positionStore.get(consumePartition);
        // 消费位置对象为空时，无此位置信息抛出异常
        if (position == null) {
            throw new JoyQueueException(JoyQueueCode.CONSUME_POSITION_NULL, "topic=" + topic + ",app=" + app + ",partition=" + partition);
        }
        return position.getPullCurIndex();
    }

    /**
     * 更新指定分区的拉取消息序号
     *
     * @param topic     消费主题
     * @param app       消费应用
     * @param partition 消费分区
     * @param index     起始消息序号
     * @return 是否更新成功
     */
    public boolean updateLastMsgPullIndex(TopicName topic, String app, short partition, long index) throws JoyQueueException {
        logger.debug("Update last pull index, topic:{}, app:{}, partition:{}, index:{}", topic, app, partition, index);
        ConsumePartition consumePartition = new ConsumePartition(topic.getFullName(), app, partition);
        Position position = positionStore.get(consumePartition);
        if (position != null) {
            position.setPullCurIndex(index);
        } else {
            logger.error("Position is null, topic:{}, app:{}, partition:{}, index:{}", topic, app, partition, index);
            // 补偿逻辑：如果当前broker是指定partition对应partitionGroup的leader，则按照给定index初始化Position，否则不处理
            addAndUpdatePosition(topic, app, partition, index);
        }
        return true;
    }


    /**
     * 获取指定分区写入的最大消息序号
     *
     * @param topic     消费主题
     * @param partition 消费分区
     * @return 指定分区已经消费到的消息序号
     */
    private long getMaxMsgIndex(TopicName topic, short partition, int groupId) {
        try {
            PartitionGroupStore store = storeService.getStore(topic.getFullName(), groupId);
            long rightIndex = store.getRightIndex(partition);
            return rightIndex;
        } catch (Exception e) {
            logger.error("getMaxMsgIndex exception, topic: {}, partition: {}, groupId: {}", topic, partition, groupId);
            return 0;
        }
    }

    protected void checkState() {
        if (!isStarted()) {
            throw new IllegalStateException("offset manager was stopped");
        }
    }

    /**
     * 新增订阅者，用于记录第一次订阅时开始的位置
     * 或运行时动态增加分区时需要初始化消费位置
     *
     * @param topic 主题
     * @param app   消费者
     */
    public void addConsumer(final TopicName topic, final String app) {
        if (topic == null || app == null || app.isEmpty()) {
            return;
        }
        checkState();
        // 从元数据中获取分组和分区数据，初始化拉取和应答位置
        List<Short> partitionList = clusterManager.getReplicaPartitions(topic);

        logger.debug("add consumer partitionList:[{}]", partitionList.toString());

        partitionList.stream().forEach(partition -> {
            ConsumePartition consumePartition = new ConsumePartition(topic.getFullName(), app, partition);

            // 获取partitionGroup
            Integer partitionGroupId = clusterManager.getPartitionGroupId(topic, partition);
            consumePartition.setPartitionGroup(partitionGroupId);

            // 获取当前（主题+分区）的最大消息序号
            long currentIndex = getMaxMsgIndex(topic, partition, partitionGroupId);
            currentIndex = Math.max(currentIndex, 0);
            // 为新订阅的应用初始化消费位置对象
            Position position = new Position(currentIndex, currentIndex, currentIndex, currentIndex);
            positionStore.putIfAbsent(consumePartition, position);

            logger.debug("Add ConsumePartition by topic:{}, app:{}, partition:{}, curIndex:{}", topic.getFullName(), app, partition, currentIndex);
        });
        // 落盘
        positionStore.forceFlush();
    }

    /**
     * 移除订阅者
     *
     * @param topic 主题
     * @param app   消费者
     */
    public void removeConsumer(final TopicName topic, final String app) {
        if (topic == null || app == null || app.isEmpty()) {
            return;
        }
        checkState();
        // 从元数据中获取分组和分区数据，初始化拉取和应答位置
        List<Short> partitionList = clusterManager.getPartitionList(topic);

        logger.debug("remove consumer partitionList:[{}]", partitionList.toString());

        partitionList.stream().forEach(partition -> {
            ConsumePartition consumePartition = new ConsumePartition(topic.getFullName(), app, partition);
            Position remove = positionStore.remove(consumePartition);

            logger.info("Remove ConsumePartition by topic:{}, app:{}, partition:{}, curIndex:{}",
                    consumePartition.getTopic(), consumePartition.getApp(), consumePartition.getPartition(), String.valueOf(remove));
        });
        // 落盘
        positionStore.forceFlush();
    }

    /**
     * 获取消费位置
     *
     * @param topic     主题
     * @param app       应用
     * @param partition 分区
     * @return
     */
    public Position getPosition(TopicName topic, String app, short partition) {
        ConsumePartition consumePartition = new ConsumePartition(topic.getFullName(), app, partition);
        return positionStore.get(consumePartition);
    }

    /**
     * 指定主题、消费者、分区添加消费位置
     *
     * @param topic          主题
     * @param partitionGroup 分区分组
     */
    private void addPartitionGroup(TopicName topic, PartitionGroup partitionGroup) {
        List<String> appList = clusterManager.getAppByTopic(topic);
        Set<Short> partitions = partitionGroup.getPartitions();

        logger.debug("add partitionGroup appList:[{}], partitions:[{}]", appList.toString(), partitions.toString());
        AtomicBoolean changed = new AtomicBoolean(false);
        partitions.stream().forEach(partition -> {
            // 获取当前（主题+分区）的最大消息序号
            long currentIndex = getMaxMsgIndex(topic, partition, partitionGroup.getGroup());
            long currentIndexVal = Math.max(currentIndex, 0);

            appList.stream().forEach(app -> {
                ConsumePartition consumePartition = new ConsumePartition(topic.getFullName(), app, partition);
                consumePartition.setPartitionGroup(partitionGroup.getGroup());

                // 为新订阅的应用初始化消费位置对象
                Position position = new Position(currentIndex, currentIndex, currentIndex, currentIndex);

                Position previous = positionStore.putIfAbsent(consumePartition, position);

                //如果当前应答位置大于索引的最大位置，则将最大位置设置为应答位置（当移除PartitionGroup事件没有通知到的情况下可能会出现）
                if (previous != null) {
                    long ackCurIndex = previous.getAckCurIndex();
                    if (ackCurIndex > currentIndex) {
                        previous.setAckCurIndex(currentIndex);
                        changed.set(true);
                        logger.warn("Update consume position topic:{}, app:{}, partition:{}, curIndex:{}, ackIndex:{}", topic.getFullName(), app, partition, currentIndexVal, ackCurIndex);
                    }
                } else {
                    changed.set(true);
                    logger.info("Add consume partition topic:{}, app:{}, partition:{}, curIndex:{}", topic.getFullName(), app, partition, currentIndexVal);
                }

            });
        });

        if (changed.get()) {
            positionStore.forceFlush();
        }
    }

    /**
     * 指定主题、消费者、分区移除消费位置
     *
     * @param topic          主题
     * @param partitionGroup 分区分组
     */
    private synchronized void removePartitionGroup(TopicName topic, PartitionGroup partitionGroup) {
        logger.info("remove partitionGroup topic:[{}], partitionGroup:[{}]", topic.getFullName(), partitionGroup);
        Iterator<ConsumePartition> iterator = positionStore.iterator();
        while(iterator.hasNext()) {
            ConsumePartition consumePartition = iterator.next();
            if (consumePartition != null &&
                    consumePartition.getPartitionGroup() == partitionGroup.getGroup() &&
                    StringUtils.equals(consumePartition.getTopic(), topic.getFullName())) {

                iterator.remove();
                logger.info("Remove ConsumePartition by topic:{}, app:{}, partition:{}", consumePartition.getTopic(), consumePartition.getApp(), consumePartition.getPartition());
            }
        }
        positionStore.forceFlush();
    }

    protected void tryForceFlush() {
        if (config.getIndexFlushInterval() <= 0) {
            positionStore.forceFlush();
            return;
        }

        long now = SystemClock.now();
        long lastFlushTimestamp = this.lastFlushIndexTimestamp.get();
        if (now - lastFlushTimestamp < config.getIndexFlushInterval()) {
            return;
        }
        if (!this.lastFlushIndexTimestamp.compareAndSet(lastFlushTimestamp, now)) {
            return;
        }
        flushIndexThread.execute(() -> {
            positionStore.forceFlush();
        });
    }

    /**
     * 添加事件
     */
    class AddConsumeListener implements EventListener<MetaEvent> {

        @Override
        public void onEvent(MetaEvent event) {
            try {
                if (event.getEventType() == EventType.ADD_CONSUMER) {
                    AddConsumerEvent addConsumerEvent = (AddConsumerEvent) event;
                    logger.info("listen add consume event:[{}]", addConsumerEvent.toString());

                    addConsumer(addConsumerEvent.getTopic(), addConsumerEvent.getConsumer().getApp());
                }
            } catch (Exception ex) {
                logger.error("AddConsumeListener error.", ex);
            }
        }
    }

    /**
     * 移除事件
     */
    class RemoveConsumeListener implements EventListener<MetaEvent> {

        @Override
        public void onEvent(MetaEvent event) {
            try {
                if (event.getEventType() == EventType.REMOVE_CONSUMER) {
                    RemoveConsumerEvent removeConsumerEvent = (RemoveConsumerEvent) event;
                    logger.info("listen remove consume event:[{}]", removeConsumerEvent.toString());

                    removeConsumer(removeConsumerEvent.getTopic(), removeConsumerEvent.getConsumer().getApp());
                }
            } catch (Exception ex) {
                logger.error("RemoveConsumeListener error.", ex);
            }
        }
    }

    /**
     * 添加分区分组事件
     */
    class AddPartitionGroupListener implements EventListener<MetaEvent> {

        @Override
        public void onEvent(MetaEvent event) {
            try {
                if (event.getEventType() == EventType.ADD_PARTITION_GROUP) {
                    AddPartitionGroupEvent addPartitionGroupEvent = (AddPartitionGroupEvent) event;
                    logger.info("listen add partition group event:[{}]", addPartitionGroupEvent.toString());

                    addPartitionGroup(addPartitionGroupEvent.getTopic(), addPartitionGroupEvent.getPartitionGroup());
                }
            } catch (Exception ex) {
                logger.error("AddPartitionGroupListener error.", ex);
            }
        }
    }

    /**
     * 删除分区分组事件
     */
    class RemovePartitionGroupListener implements EventListener<MetaEvent> {

        @Override
        public void onEvent(MetaEvent event) {
            try {
                if (event.getEventType() == EventType.REMOVE_PARTITION_GROUP) {
                    RemovePartitionGroupEvent removePartitionGroupEvent = (RemovePartitionGroupEvent) event;
                    logger.info("listen remove partition group event:[{}]", removePartitionGroupEvent.toString());

                    removePartitionGroup(removePartitionGroupEvent.getTopic(), removePartitionGroupEvent.getPartitionGroup());
                }
            } catch (Exception ex) {
                logger.error("RemovePartitionGroupListener error.", ex);
            }

        }
    }

    /**
     * 更新分区分组事件(添加分区或减少分区)
     */
    class UpdatePartitionGroupListener implements EventListener<MetaEvent> {

        @Override
        public void onEvent(MetaEvent event) {
            try {
                if (event.getEventType() == EventType.UPDATE_PARTITION_GROUP) {
                    UpdatePartitionGroupEvent updatePartitionGroupEvent = (UpdatePartitionGroupEvent) event;

                    logger.info("listen update partition group event:[{}]", updatePartitionGroupEvent.toString());

                    TopicName topic = updatePartitionGroupEvent.getTopic();
                    PartitionGroup newPartitionGroup = updatePartitionGroupEvent.getNewPartitionGroup();

                    Set<Short> newPartitionSet = clusterManager.getTopicConfig(topic).fetchAllPartitions();

                    Iterator<ConsumePartition> iterator = positionStore.iterator();
                    while (iterator.hasNext()) {
                        ConsumePartition next = iterator.next();
                        if (StringUtils.equals(next.getTopic(), topic.getFullName()) && /* 不在最新分区集合中 */ !newPartitionSet.contains(next.getPartition())) {
                            // 缓存中的分区位置信息，不在最新的分区集合中，则删除
                            iterator.remove();
                        }
                    }

                    addPartitionGroup(topic, newPartitionGroup);
                }
            } catch (Exception ex) {
                logger.error("UpdatePartitionGroupListener error.", ex);
            }
        }
    }
}
