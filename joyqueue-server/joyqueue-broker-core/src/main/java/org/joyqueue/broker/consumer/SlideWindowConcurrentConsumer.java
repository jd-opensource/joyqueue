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
package org.joyqueue.broker.consumer;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.joyqueue.broker.archive.ArchiveManager;
import org.joyqueue.broker.archive.ConsumeArchiveService;
import org.joyqueue.broker.buffer.Serializer;
import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.consumer.model.ConsumePartition;
import org.joyqueue.broker.consumer.model.PullResult;
import org.joyqueue.broker.consumer.position.PositionManager;
import org.joyqueue.broker.event.BrokerEventBus;
import org.joyqueue.domain.Partition;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.TopicName;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.exception.JoyQueueException;
import org.joyqueue.message.BrokerMessage;
import org.joyqueue.message.MessageLocation;
import org.joyqueue.network.session.Connection;
import org.joyqueue.network.session.Consumer;
import org.joyqueue.server.retry.api.MessageRetry;
import org.joyqueue.server.retry.model.RetryMessageModel;
import org.joyqueue.store.PartitionGroupStore;
import org.joyqueue.store.PositionOverflowException;
import org.joyqueue.store.PositionUnderflowException;
import org.joyqueue.store.ReadResult;
import org.joyqueue.store.StoreService;
import org.joyqueue.store.event.StoreNodeChangeEvent;
import org.joyqueue.toolkit.concurrent.CasLock;
import org.joyqueue.toolkit.concurrent.EventListener;
import org.joyqueue.toolkit.concurrent.NamedThreadFactory;
import org.joyqueue.toolkit.format.Format;
import org.joyqueue.toolkit.lang.Close;
import org.joyqueue.toolkit.network.IpUtil;
import org.joyqueue.toolkit.service.Service;
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.joyqueue.exception.JoyQueueCode.CONSUME_POSITION_UPDATE_ERROR;

/**
 * 基于滑动窗口的并行消费实现。
 *
 * 实现思路是，在每个开启并行消费的分区上，维护一个滑动窗口（SliceWindow），随着不断的拉取和ACK消息，滑动窗口在分区上持续的向后滑动。
 * 每个滑动窗口中包含若干连续、有序、无间隔的消息片段（ConsumedMessages），每个消息片段包含分区上连续的若干条消息。
 * 消息片段是并行消费的基本单位，每次客户端来拉取消息的时候，返回一个消息片段。每次客户端返回ACK也对应一个消息片段。
 *
 * 客户端来拉取消息时，优先返回滑动窗口中现有的消息片段中超时未ACK的片段，如果没有，则向后扩展滑动窗口，增加一个消息片段返回给客户端。
 * 客户端来ACK消息时，找到对应的消息片段，并将状态置为ACK（无论是否已超时）。
 * 然后从滑动窗口头部开始检查是否有连续的、已ACK的消息片段，如果有，更新PositionManager中的ACK位置，并删除这些已ACK的消息片段，
 * 同时滑动窗口向前收缩。
 *
 *
 * @author LiYue
 * Date: 2020/4/9
 */
public class SlideWindowConcurrentConsumer extends Service implements ConcurrentConsumer {

    private static final Logger logger = LoggerFactory.getLogger(SlideWindowConcurrentConsumer.class);

    // 分区管理
    private PartitionManager partitionManager;
    // 重试管理
    private MessageRetry messageRetry;
    // 消费位置管理
    private PositionManager positionManager;
    // 存储服务
    private StoreService storeService;
    // 集群管理
    private ClusterManager clusterManager;
    // 延期帮助类
    private FilterMessageSupport filterMessageSupport;
    // 重启进程需要重置拉取消息位置，这里维护是否已重置;K=分区,V=是否已重置
    private ConcurrentMap<ConsumePartition, Boolean> resetPullPositionFlag = new ConcurrentHashMap<>();

    // 延迟处理器
    private DelayHandler delayHandler = new DelayHandler();
    // 消费归档服务
    private ArchiveManager archiveManager;
    private ConsumeConfig consumeConfig;
    private BrokerEventBus brokerEventBus;

    private static final long CLEAN_INTERVAL_SEC = 60L;

    private final Map<ConsumePartition, SlideWindow> slideWindowMap = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduledExecutorService;

    SlideWindowConcurrentConsumer(ClusterManager clusterManager, StoreService storeService, PartitionManager partitionManager,
                                  MessageRetry messageRetry, PositionManager positionManager, FilterMessageSupport filterMessageSupport, ArchiveManager archiveManager,
                                  ConsumeConfig consumeConfig, BrokerEventBus brokerEventBus) {
        this.clusterManager = clusterManager;
        this.storeService = storeService;
        this.partitionManager = partitionManager;
        this.messageRetry = messageRetry;
        this.positionManager = positionManager;
        this.filterMessageSupport = filterMessageSupport;
        this.archiveManager = archiveManager;
        this.consumeConfig = consumeConfig;
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("ConcurrentConsumerClearExecutor", true));
        this.brokerEventBus = brokerEventBus;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        // 定时清理已关闭并行消费或者已经失效的主题
        scheduledExecutorService.scheduleAtFixedRate(this::clearSlideWindow, CLEAN_INTERVAL_SEC + 32, CLEAN_INTERVAL_SEC, TimeUnit.MILLISECONDS);
        brokerEventBus.addListener(new EventListener() {
            @Override
            public void onEvent(Object event) {
                if (event instanceof StoreNodeChangeEvent) {
                    onNodeChangeEvent((StoreNodeChangeEvent) event);
                }
            }
        });
        logger.info("SlideWindowConcurrentConsumer is started.");
    }

    protected void onNodeChangeEvent(StoreNodeChangeEvent event) {
        if (event.getNodes().getRWNode() == null || event.getNodes().getRWNode().getId() != clusterManager.getBrokerId()) {
            clearSlideWindow(event.getTopic(), event.getGroup());
        }
    }

    protected void clearSlideWindow() {
        Iterator<Map.Entry<ConsumePartition, SlideWindow>> iterator = slideWindowMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<ConsumePartition, SlideWindow> entry = iterator.next();
            ConsumePartition consumePartition = entry.getKey();

            if (clearSlideWindow(consumePartition)) {
                iterator.remove();
            }
        }
    }

    protected void clearSlideWindow(String topic, int group) {
        PartitionGroup partitionGroup = clusterManager.getPartitionGroupByGroup(TopicName.parse(topic), group);
        if (partitionGroup == null) {
            return;
        }
        Set<Short> partitions = partitionGroup.getPartitions();
        Iterator<Map.Entry<ConsumePartition, SlideWindow>> iterator = slideWindowMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<ConsumePartition, SlideWindow> entry = iterator.next();
            ConsumePartition consumePartition = entry.getKey();

            if (consumePartition.getTopic().equals(topic) && partitions.contains(consumePartition.getPartition())) {
                if (clearSlideWindow(consumePartition)) {
                    iterator.remove();
                }
            }
        }
    }

    protected boolean clearSlideWindow(ConsumePartition consumePartition) {
        boolean isLeader = clusterManager.isLeader(consumePartition.getTopic(), consumePartition.getPartition());
        org.joyqueue.domain.Consumer.ConsumerPolicy policy = clusterManager.tryGetConsumerPolicy(TopicName.parse(consumePartition.getTopic()), consumePartition.getApp());
        if (policy != null && policy.isConcurrent() && isLeader) {
            return false;
        }
        resetPullPositionFlag.remove(consumePartition);
        return true;
    }

    @Override
    protected void doStop() {
        super.doStop();
        Close.close(scheduledExecutorService);
        logger.info("SlideWindowConcurrentConsumer is stopped.");
    }

    /**
     * 读取消息
     * <br>
     *   首先尝试消费高优先级队列消息
     * <br>
     *   再轮询消费(普通+重试(optional)）队列
     * <br>
     *
     * @param consumer    消费者信息
     * @param count       消息条数
     * @param accessTimes 访问次数用于均匀读取每个分区
     * @return 读取的消息
     */
    @Override
    public PullResult getMessage(Consumer consumer, int count, long ackTimeout, long accessTimes, int concurrent) throws JoyQueueException {
        // 消费普通分区消息
        PullResult pullResult = null;
        List<Short> priorityPartitionList = partitionManager.getPriorityPartition(TopicName.parse(consumer.getTopic()));
        if (priorityPartitionList.size() > 0) {
            // 高优先级分区消费
            pullResult = getFromPartition(consumer, priorityPartitionList, count, ackTimeout, accessTimes, concurrent);
        }
        if (pullResult == null || pullResult.isEmpty()) {
            List<Short> partitionList = clusterManager.getLocalPartitions(TopicName.parse(consumer.getTopic()));
            if (partitionManager.isRetry(consumer)) {
                partitionList = new ArrayList<>(partitionList);
                partitionList.add(Partition.RETRY_PARTITION_ID);
            }
            pullResult = getFromPartition(consumer, partitionList, count, ackTimeout, accessTimes, concurrent);
        }
        return pullResult;
    }

    private PullResult getRetryMessages(Consumer consumer, short count) throws JoyQueueException {
        PullResult pullResult;// 消费待重试的消息
        List<RetryMessageModel> retryMessage = messageRetry.getRetry(consumer.getTopic(), consumer.getApp(), count, 0);
        List<ByteBuffer> messages = convert(retryMessage);
        pullResult = new PullResult(consumer, (short) -1, new ArrayList<>(messages.size()));
        pullResult.setBuffers(messages);
        if (messages.size() > 0) {
            // 读到重试消息，增加下次读到重试队列的概率（优先处理重试消息）
            partitionManager.increaseRetryProbability(consumer);
        } else {
            // 读不到重试消息，则降低下次读重试队列的概率
            partitionManager.decreaseRetryProbability(consumer);
        }
        return pullResult;
    }

    /**
     * broker内部应答的APP名称
     */
    private static final String innerAppPrefix = "innerFilter@";

    /**
     * 消息归档
     *
     * @param messageLocations 消息位置
     * @throws JoyQueueException 发生业务异常时抛出
     */
    private void archiveIfNecessary(MessageLocation[] messageLocations) throws JoyQueueException{
        ConsumeArchiveService archiveService;

        if (archiveManager == null || (archiveService = archiveManager.getConsumeArchiveService()) == null) {
            return;
        }
        Connection connection= new Connection();
        try {
            connection.setAddress(IpUtil.toByte(new InetSocketAddress(IpUtil.getLocalIp(), 50088)));
        } catch (Exception ex) {
            // 如果获取本机IP为空，则添加0长度的byte数组
            connection.setAddress(new byte[0]);
        }

        connection.setApp(innerAppPrefix + connection.getApp());

        archiveService.appendConsumeLog(connection, messageLocations);

    }

    /**
     * 将消息集合转换为应答位置数组
     *
     */
    private MessageLocation[] convertMessageLocation(String topic, List<ByteBuffer> inValidList) {
        MessageLocation[] locations = new MessageLocation[inValidList.size()];
        for (int i = 0; i < inValidList.size(); i++) {
            ByteBuffer buffer = inValidList.get(i);
            short partition = Serializer.readPartition(buffer);
            long index = Serializer.readIndex(buffer);
            locations[i] = new MessageLocation(topic, partition, index);
        }
        return locations;
    }

    /**
     * 将BrokerMessage转成RByteBuffer
     *
     */
    private List<ByteBuffer> convert(List<RetryMessageModel> retryMessage) throws JoyQueueException {
        if (CollectionUtils.isEmpty(retryMessage)) {
            return new ArrayList<>(0);
        }

        List<ByteBuffer> rst = new ArrayList<>(retryMessage.size());
        for (RetryMessageModel message : retryMessage) {
            try {
                ByteBuffer wrap = ByteBuffer.wrap(message.getBrokerMessage());
                Serializer.setPartition(wrap, Partition.RETRY_PARTITION_ID);
                Serializer.setIndex(wrap, message.getIndex());
                rst.add(wrap);
            } catch (Exception e) {
                throw new JoyQueueException(JoyQueueCode.SE_IO_ERROR, e);
            }
        }

        return rst;
    }

    /**
     * 从本地磁盘分区并行(不锁队列)消费消息，优先消费超时未ack的消息块
     *
     * @param consumer      消费者信息
     * @param partitionList 分区集合
     * @param count         消息条数
     * @param ackTimeout    应答超时
     * @param accessTimes   访问次数
     * @return 拉取消息对象
     */
    private PullResult getFromPartition(Consumer consumer, List<Short> partitionList, int count, long ackTimeout, long accessTimes, int concurrent) throws JoyQueueException {
        int partitionSize = partitionList.size();
        int listIndex = -1;

        int retryMax = consumeConfig.getPartitionSelectRetryMax();

        PullResult pullResult = new PullResult(consumer, (short) -1, new ArrayList<>(0));
        for (int i = 0; i < partitionSize; i++) {
            if (i == retryMax) {
                break;
            }
            listIndex = partitionManager.selectPartitionIndex(partitionSize, listIndex + i, accessTimes);
            short partition = partitionList.get(listIndex);
            ConsumePartition consumePartition = new ConsumePartition(consumer.getTopic(), consumer.getApp(), partition);
            SlideWindow slideWindow = slideWindowMap.computeIfAbsent(consumePartition,
                    k -> {
                        try {
                            return new SlideWindow(
                                    getPullIndex(consumer, k.getPartition()));
                        } catch (JoyQueueException e) {
                            throw new RuntimeException(e);
                        }
                    });


            // 尝试获取并锁定超时未响应的片段
            ConsumedMessages consumedMessages = slideWindow.tryGetAndLockExpired(ackTimeout);
            if (null != consumedMessages) {
                ReadMessagesResult readMessagesResult = readMessages(consumer, partition, consumedMessages.getStartIndex(), consumedMessages.getCount());
                pullResult = readMessagesResult.getPullResult();
                break;
            }

            //超过并行度取下一个Partition
            if (slideWindow.concurrentCount() >= concurrent) {
                continue;
            }

            if(slideWindow.getAppendLock().tryLock()) {
                try {
                    if (partition == Partition.RETRY_PARTITION_ID) {
                        pullResult = getRetryMessages(consumer, (short) 1);
                        if (pullResult != null && !pullResult.isEmpty()) {
                            return pullResult;
                        } else {
                            continue;
                        }
                    }

                    // 获取消息拉取位置
                    long pullIndex = getPullIndex(consumer, partition);

                    logger.debug("get pull index:{}, topic:{}, app:{}, partition:{}", pullIndex, consumer.getTopic(), consumer.getApp(), partition);
                    // 读取消息结果

                    ReadMessagesResult readMessagesResult = readMessages(consumer, partition, pullIndex, count);
                    pullResult = readMessagesResult.getPullResult();
                    List<ByteBuffer> pullMessages = pullResult.getBuffers();
                    List<ByteBuffer> filteredMessages = readMessagesResult.getFilteredMessages();
                    if(null == filteredMessages) {
                        filteredMessages = new ArrayList<>(0);
                    }
                    // 如果当前分区没有消息，取下一个分区
                    if (pullMessages.size() + filteredMessages.size() == 0) {
                        continue;
                    }

                    long pullMessageStartIndex = pullMessages.size() > 0 ? Serializer.readIndex(pullMessages.get(0)) : -1L;
                    long filteredMessageStartIndex = filteredMessages.size() > 0 ? Serializer.readIndex(filteredMessages.get(0)) : -1L;

                    // 返回的正常消息和过滤的消息这两批消息，先后顺序可能不同，但必须满足：
                    // 1. 这两批消息在分区上的索引序号必须是连续的，中间不能有间隔；
                    // 2. 第一批消息的第一条消息，索引序号必须等于上面调用readMessages方法参数中的pullIndex

                    if(filteredMessageStartIndex == -1L || pullMessageStartIndex < filteredMessageStartIndex) {
                        int msgCount = count(pullMessages);

                        // 检查二段消息是否连续
                        if(filteredMessageStartIndex >= 0 && filteredMessageStartIndex != pullMessageStartIndex + msgCount) {
                            // 不应该走到这儿，除非是Bug了。
                            throw new JoyQueueException(
                                    String.format("Index not match, pullIndex: %s, pullMessageStartIndex: %s, " +
                                                    "filteredMessageStartIndex: %s, topic: %s, partition: %d!",
                                            Format.formatWithComma(pullIndex),
                                            Format.formatWithComma(pullMessageStartIndex),
                                            Format.formatWithComma(filteredMessageStartIndex),
                                            consumer.getTopic(),
                                            partition),
                                    CONSUME_POSITION_UPDATE_ERROR.getCode()
                            );
                        }
                        // 先处理正常消息
                        if (msgCount > 0) {
                            boolean extended = extendSlideWindowAndUpdatePullIndex(
                                    pullMessageStartIndex, msgCount, consumer, partition, ackTimeout, false, slideWindow);
                            if (!extended) {
                                continue;
                            }
                        }

                        // 再处理过滤消息
                        if(filteredMessageStartIndex >= 0) {
                            int filterMessageCount = count(filteredMessages);
                            if (filterMessageCount > 0) {
                                boolean extended = extendSlideWindowAndUpdatePullIndex(
                                        filteredMessageStartIndex, filterMessageCount, consumer, partition, ackTimeout, true, slideWindow);
                                if (extended) {
                                    // 归档
                                    MessageLocation[] messageLocations = convertMessageLocation(consumer.getTopic(), filteredMessages);
                                    archiveIfNecessary(messageLocations);
                                }
                            }
                        }

                        // 如果读到消息退出循环
                        if (msgCount > 0) {
                            break;
                        }
                    } else {
                        int filterMessageCount = count(filteredMessages);

                        // 检查二段消息是否连续
                        if(pullMessageStartIndex >= 0 && pullMessageStartIndex != filteredMessageStartIndex + filterMessageCount) {
                            // 不应该走到这儿，除非是Bug了。
                            throw new JoyQueueException(
                                    String.format("Index not match, pullIndex: %s, pullMessageStartIndex: %s, " +
                                                    "filteredMessageStartIndex: %s, topic: %s, partition: %d!",
                                            Format.formatWithComma(pullIndex),
                                            Format.formatWithComma(pullMessageStartIndex),
                                            Format.formatWithComma(filteredMessageStartIndex),
                                            consumer.getTopic(),
                                            partition),
                                    CONSUME_POSITION_UPDATE_ERROR.getCode()
                            );
                        }

                        // 先处理过滤消息
                        if (filterMessageCount > 0) {
                            boolean extended = extendSlideWindowAndUpdatePullIndex(
                                    filteredMessageStartIndex, filterMessageCount, consumer, partition, ackTimeout, true, slideWindow);
                            if(extended) {
                                // 归档
                                MessageLocation[] messageLocations = convertMessageLocation(consumer.getTopic(), filteredMessages);
                                archiveIfNecessary(messageLocations);
                            } else {
                                continue;
                            }
                        }

                        // 再处理正常消息
                        int msgCount = count(filteredMessages);
                        if (msgCount > 0) {
                            boolean extended = extendSlideWindowAndUpdatePullIndex(
                                    pullMessageStartIndex, msgCount, consumer, partition, ackTimeout, false, slideWindow);
                            if (!extended) {
                                continue;
                            }
                        }

                        // 如果读到消息退出循环
                        if (msgCount > 0) {
                            break;
                        }

                    }
                } finally {
                    slideWindow.getAppendLock().unlock();
                }
            }

        }
        return pullResult;
    }

    /**
     * 在尾部扩展滑动窗口，并更新拉取位置
     * @param pullIndex 当前拉取位置
     * @param msgCount 消息条数
     * @param consumer 消费者
     * @param partition 分区
     * @param ackTimeout ack超时时长
     * @param ack 是否直接ack，用于过滤的消息。
     * @param slideWindow 滑动窗口
     * @return 是否执行成功
     * @throws JoyQueueException 发生业务异常的时候抛出
     */
    private boolean extendSlideWindowAndUpdatePullIndex(
            long pullIndex, int msgCount, Consumer consumer, short partition, long ackTimeout,
            boolean ack, SlideWindow slideWindow) throws JoyQueueException{
        ConsumedMessages consumedMessages;
        if (null != (consumedMessages = slideWindow.appendUnsafe(consumer.getTopic(), partition, pullIndex, msgCount, ackTimeout))) {
            if (ack) {
                consumedMessages.ack();
            }
            // 更新最新拉取位置，即下次开始拉取的序号
            long newPullIndex = pullIndex + msgCount;
            if (logger.isDebugEnabled()) {
                logger.debug("set new pull index:{}, topic:{}, app:{}, partition:{}", newPullIndex, consumer.getTopic(), consumer.getApp(), partition);
            }
            if (consumedMessages.isReset()) {
                positionManager.updateLastMsgAckIndex(TopicName.parse(consumer.getTopic()), consumer.getApp(), partition, pullIndex);
            }
            positionManager.updateLastMsgPullIndex(TopicName.parse(consumer.getTopic()), consumer.getApp(), partition, newPullIndex);
            return true;
        } else {
            return false;
        }

    }

    private int count(List<ByteBuffer> buffers) {
        int count = 0;
        for (ByteBuffer buffer : buffers) {
            BrokerMessage header = Serializer.readBrokerMessageHeader(buffer);
            if (header.isBatch()) {
                count += header.getFlag();
            } else {
                count += 1;
            }
        }
        return count;
    }

    /**
     * 进程重启后更新消息拉取位置
     *
     * @param consumer  消费者
     * @param partition 分区
     * @return 消息序号
     */
    private long getPullIndex(Consumer consumer, short partition) throws JoyQueueException {
        if (partition == Partition.RETRY_PARTITION_ID) {
            return 0;
        }
        // 本次拉取消息的位置，默认从0开始ack
        long pullIndex = 0;
        String topic = consumer.getTopic();
        String app = consumer.getApp();
        ConsumePartition consumePartition = new ConsumePartition(topic, app, partition);
        TopicName topicName = TopicName.parse(topic);
        // 判断是否已经初始化
        Boolean isReset = resetPullPositionFlag.get(consumePartition);
        if (isReset != null && isReset) {
            // 已经初始化，直接获取拉取到的序号
            pullIndex = positionManager.getLastMsgPullIndex(topicName, app, partition);
            if (pullIndex == -1) {
                positionManager.updateLastMsgPullIndex(topicName, app, partition, positionManager.getLastMsgAckIndex(topicName, app, partition));
            }
        } else {
            // 还没有初始化，重置拉取位置
            // 主题+应用+分组+分区的最后应答位置
            long lastAckIndex = positionManager.getLastMsgAckIndex(topicName, app, partition);
            // 用最后应答位置更新拉取位置
            boolean isSuccess = positionManager.updateLastMsgPullIndex(topicName, app, partition, lastAckIndex);
            if (isSuccess) {
                // 设置已完成拉取位置初始化
                resetPullPositionFlag.put(consumePartition, Boolean.TRUE);
                pullIndex = lastAckIndex;
            }

            logger.info("init concurrent pull topic {}, app {}, partition {}, index [{}]", consumer.getTopic(), consumer.getApp(), partition, pullIndex);
        }

        return pullIndex;
    }

    /**
     * 指定分组、分区、序号读取消息
     * <br>
     * 处理分区占用<br>
     * 延迟消费问题
     *
     * @param consumer  消费者信息
     * @param partition 消费分区
     * @param index     消息序号
     * @param count     消息条数
     * @return 读取的消息
     */
    private ReadMessagesResult readMessages(Consumer consumer, short partition, long index, int count) {
        // 初始化默认
        ReadMessagesResult readMessagesResult = new ReadMessagesResult();
        PullResult pullResult = new PullResult(consumer, partition, new ArrayList<>(0));
        try {
            int partitionGroup = clusterManager.getPartitionGroupId(TopicName.parse(consumer.getTopic()), partition);
            PartitionGroupStore store = storeService.getStore(consumer.getTopic(), partitionGroup);
            ReadResult readRst = store.read(partition, index, count, Long.MAX_VALUE);
            if (readRst.getCode() == JoyQueueCode.SUCCESS) {
                if (readRst.getMessages() != null) {
                    pullResult.setBuffers(Lists.newArrayList(readRst.getMessages()));
                }
//                List<ByteBuffer> byteBufferList = Lists.newArrayList(readRst.getMessages());
//                org.joyqueue.domain.Consumer consumerConfig = clusterManager.getConsumer(TopicName.parse(consumer.getTopic()), consumer.getApp());
//
//                if (consumerConfig != null) {
//                    // 过滤消息
//                    List<ByteBuffer> byteBuffers = filterMessageSupport.filter(consumerConfig, byteBufferList, readMessagesResult::setFilteredMessages);
//
//                    // 开启延迟消费，过滤未到消费时间的消息
//                    byteBuffers = delayHandler.handle(consumerConfig.getConsumerPolicy(), byteBuffers);
//                    // 构建拉取结果
//                    pullResult = new PullResult(consumer, partition, byteBuffers);
//                }
            } else {
                logger.error("read message error, error code[{}]", readRst.getCode());
            }
        } catch (PositionOverflowException e) {
            if(e.getRight() < index) {
                pullResult.setCode(JoyQueueCode.SE_INDEX_OVERFLOW);
            }
        } catch (PositionUnderflowException e) {
            pullResult.setCode(JoyQueueCode.SE_INDEX_UNDERFLOW);
        } catch (Exception ex) {
            logger.error("get message error, consumer: {}, partition: {}", consumer, partition, ex);
        }
        readMessagesResult.setPullResult(pullResult);
        return readMessagesResult;
    }

    /**
     * 消息消费应答
     *
     * @param locations    应答位置信息
     * @param consumer     消费者
     * @param isSuccessAck 是否成功再应答
     * @return 是否应答成功
     * @throws JoyQueueException 出现业务异常
     */
    @Override
    public boolean acknowledge(MessageLocation[] locations, Consumer consumer, boolean isSuccessAck) throws JoyQueueException {
        boolean isSuccess = false;
        if (locations.length < 1) {
            return false;
        }
        String topic = consumer.getTopic();
        String app = consumer.getApp();
        short partition = locations[0].getPartition();
        // 重试应答
        if (partition == Partition.RETRY_PARTITION_ID) {
            return retryAck(topic, app, locations, isSuccessAck);
        }

        long[] locationArray = new long[locations.length];
        for (int i = 0; i < locations.length; i++) {
            locationArray[i] = locations[i].getIndex();
        }
        logger.debug("pre ack, partition: {}, index: {}", partition, locationArray);

        // 连续顺序
        long[] indexArr = AcknowledgeSupport.sortMsgLocation(locations);

        if (indexArr != null) {
            ConsumePartition consumePartition = new ConsumePartition(topic, app, partition);
            SlideWindow slideWindow = slideWindowMap.get(consumePartition);
            if(null != slideWindow) {
                long startIndex = indexArr[0];
                int count = (int) (indexArr[1] - indexArr[0] + 1);
                isSuccess = slideWindow.ack(
                        TopicName.parse(topic), app, partition, startIndex, count, positionManager);
            }
        }

        return isSuccess;
    }

    /**
     * 重试应答
     *
     */
    private boolean retryAck(String topic, String app, MessageLocation[] locations, boolean isSuccess) {
        Long[] indexArr = new Long[locations.length];
        for (int i = 0; i < locations.length; i++) {
            indexArr[i] = locations[i].getIndex();
        }
        try {
            if (isSuccess) {
                messageRetry.retrySuccess(topic, app, indexArr);
            } else {
                messageRetry.retryError(topic, app, indexArr);
            }
        } catch (JoyQueueException e) {
            logger.error("RetryAck error.", e);
            return false;
        }
        return true;
    }

    private static class ReadMessagesResult {
        private PullResult pullResult;
        private List<ByteBuffer> filteredMessages;

        PullResult getPullResult() {
            return pullResult;
        }

        void setPullResult(PullResult pullResult) {
            this.pullResult = pullResult;
        }

        List<ByteBuffer> getFilteredMessages() {
            return filteredMessages;
        }

        void setFilteredMessages(List<ByteBuffer> filteredMessages) {
            this.filteredMessages = filteredMessages;
        }
    }

    /**
     * 在分区上的并行消费滑动窗口
     */
    private static class SlideWindow {
        private final NavigableMap<Long /* index */ , ConsumedMessages> consumedMessagesMap = new ConcurrentSkipListMap<>();
        // 上一次检查是否有超时过期ConsumedMessages的时间戳
        private final AtomicLong lastCheckExpireTimestamp = new AtomicLong(0L);
        // 最小检查过期时间间隔，避免频繁遍历consumedMessagesMap
        private static final long MIN_CHECK_EXPIRE_INTERVAL_MS = 1000L;

        // 估计可用的过期片段数量，不是百分之百准确，主要用于拦截不必要的遍历
        private AtomicInteger expiredCount = new AtomicInteger(0);
        private final CasLock appendLock = new CasLock();
        // 下一次拉取的位置
        private long nextPullIndex;

        SlideWindow(long nextPullIndex) {
            this.nextPullIndex = nextPullIndex;
        }

        int concurrentCount() {
            return consumedMessagesMap.size();
        }

        ConsumedMessages tryGetAndLockExpired(long ackTimeoutMs) {
            ConsumedMessages result;
            // 如果有过期的直接尝试去找到过期的片段
            result = tryFindFirstExpired(ackTimeoutMs);

            if(lastCheckExpireTimestamp.get() + MIN_CHECK_EXPIRE_INTERVAL_MS < SystemClock.now()) {
                long timestamp = lastCheckExpireTimestamp.get();
                if (timestamp + MIN_CHECK_EXPIRE_INTERVAL_MS < SystemClock.now() &&
                        lastCheckExpireTimestamp.compareAndSet(timestamp, SystemClock.now())) {
                    // 检查超时过期的片段，并重置锁定状态
                    expiredCount.set(0);
                    for (ConsumedMessages consumedMessages : consumedMessagesMap.values()) {
                        if(consumedMessages.isExpired()) {
                            expiredCount.incrementAndGet();
                        }
                    }

                    result = tryFindFirstExpired(ackTimeoutMs);
                }
            }
            return result;
        }

        private ConsumedMessages tryFindFirstExpired(long ackTimeoutMs) {
            ConsumedMessages result = null;
            if (expiredCount.get() > 0) {
                // 返回并锁定第一个可用的片段
                result = consumedMessagesMap.values().stream()
                        .filter(cm -> cm.tryLock(ackTimeoutMs)).findFirst().orElse(null);
                if (null != result) {
                    expiredCount.decrementAndGet();
                } else {
                    expiredCount.set(0);
                }
            }
            return result;
        }

        ConsumedMessages appendUnsafe(String topic, short partition, long nextPullIndex, int count, long timeoutMs) {
            boolean isReset = false;
            if(this.nextPullIndex != nextPullIndex) { // 如果不相等，有可能是重置了消费位置，以新的消费位置为准
                logger.warn("Reset concurrent consumer pull index from {} to {}, topic: {}, partition: {}.",
                        this.nextPullIndex,
                        nextPullIndex,
                        topic,
                        partition);
                consumedMessagesMap.clear();
                this.nextPullIndex = nextPullIndex;
                isReset = true;
            }
            ConsumedMessages consumedMessages = new ConsumedMessages(nextPullIndex, count, timeoutMs, isReset);
            this.nextPullIndex += count;
            consumedMessagesMap.put(nextPullIndex, consumedMessages);
            return consumedMessages;
        }

        CasLock getAppendLock() {
            return appendLock;
        }

        private AtomicInteger counter = new AtomicInteger(0);

        boolean ack(TopicName topic, String app, short partition, long startIndex, int count, PositionManager positionManager) throws JoyQueueException {
            List<ConsumedMessages> toBeAcked = new LinkedList<>();
            boolean ret = false;
            long curIndex = startIndex;
            int remainingCount = count;
            ConsumedMessages consumedMessages;
            while (remainingCount > 0 && (consumedMessages = consumedMessagesMap.get(curIndex)) != null) {
                toBeAcked.add(consumedMessages);
                curIndex += consumedMessages.count;
                remainingCount -= consumedMessages.count;
            }
            if (remainingCount == 0 && toBeAcked.size() > 0) {
               toBeAcked.forEach(ConsumedMessages::ack);

               // 如果确认的片段是滑动窗口的第一段，需要在分区上ack，并向尾部缩小滑动窗口

               while (!consumedMessagesMap.isEmpty() && (consumedMessages = consumedMessagesMap.firstEntry().getValue()).isAcked()) {
                   long lastMsgAckIndex = positionManager.getLastMsgAckIndex(topic, app, partition);
                   consumedMessagesMap.remove(consumedMessages.getStartIndex());
                   if (lastMsgAckIndex >= consumedMessages.getStartIndex() && lastMsgAckIndex < consumedMessages.getStartIndex() + consumedMessages.getCount()) {
                       positionManager.updateLastMsgAckIndex(topic, app, partition,
                               consumedMessages.getStartIndex() + consumedMessages.getCount(), false);
                   } else {
                       logger.warn("Ack index not match, topic: {}, partition: {}, ack: [{} - {}], currentAckIndex: {}!",
                               topic.getFullName(),
                               partition,
                               Format.formatWithComma(consumedMessages.getStartIndex()),
                               Format.formatWithComma(consumedMessages.getStartIndex() + consumedMessages.getCount()),
                               Format.formatWithComma(lastMsgAckIndex)
                       );
                   }
               }
               ret = true;
            }
            if(!ret) {
                logger.warn("Concurrent consume ack failed, topic: {}, partition: {}, ack: [{} - {}), currentAckIndex: {}.",
                        topic.getFullName(),
                        partition,
                        Format.formatWithComma(startIndex),
                        Format.formatWithComma(startIndex + count),
                        Format.formatWithComma(positionManager.getLastMsgAckIndex(topic, app, partition)));
            }
            return ret;
        }
    }

    /**
     * 在某个分区上连续的n条消息，并行消费ack最小单元。每次一个客户端请求消息的时候，对应发给客户端的一个PullResult。
     * 线程安全。
     */
    private static class ConsumedMessages {
        static final int LOCKED = 0;
        static final int ACKED = 1;
        static final int EXPIRED = -1;
        private final long startIndex;
        private final int count;
        private long expireTime;
        private boolean reset;


        // 这段消息的状态
        private AtomicInteger status = new AtomicInteger(LOCKED);
        ConsumedMessages(long startIndex, int count, long timeoutMs, boolean reset) {
            this.startIndex = startIndex;
            this.count = count;
            this.expireTime = SystemClock.now() + timeoutMs;
            this.reset = reset;
        }

        int getCount() {
            return count;
        }

        long getStartIndex() {
            return startIndex;
        }

        boolean tryLock(long timeoutMs) {
            maybeUnlockExpired();
            if (status.compareAndSet(EXPIRED, LOCKED)) {
                expireTime = SystemClock.now() + timeoutMs;
                return true;
            }
            return false;
        }

        private void maybeUnlockExpired() {
            if (status.get() == LOCKED && SystemClock.now() > expireTime) {
                status.compareAndSet(LOCKED, EXPIRED);
            }
        }

        private void ack() {
            status.set(ACKED);
        }

        boolean isExpired() {
            maybeUnlockExpired();
            return status.get() == EXPIRED;
        }

        boolean isAcked() {
            return status.get() == ACKED;
        }

        boolean isReset() {
            return reset;
        }
    }
}
