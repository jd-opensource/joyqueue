/**
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
package com.jd.journalq.broker.consumer;

import com.google.common.collect.Lists;
import com.jd.journalq.broker.archive.ArchiveManager;
import com.jd.journalq.broker.archive.ConsumeArchiveService;
import com.jd.journalq.broker.buffer.Serializer;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.broker.consumer.filter.FilterCallback;
import com.jd.journalq.broker.consumer.model.ConsumePartition;
import com.jd.journalq.broker.consumer.model.PullResult;
import com.jd.journalq.broker.consumer.position.PositionManager;
import com.jd.journalq.broker.consumer.position.model.Position;
import com.jd.journalq.domain.Partition;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.exception.JMQException;
import com.jd.journalq.message.MessageLocation;
import com.jd.journalq.network.session.Connection;
import com.jd.journalq.network.session.Consumer;
import com.jd.journalq.server.retry.api.MessageRetry;
import com.jd.journalq.server.retry.model.RetryMessageModel;
import com.jd.journalq.store.PartitionGroupStore;
import com.jd.journalq.store.PositionOverflowException;
import com.jd.journalq.store.PositionUnderflowException;
import com.jd.journalq.store.ReadResult;
import com.jd.journalq.store.StoreService;
import com.jd.journalq.toolkit.network.IpUtil;
import com.jd.journalq.toolkit.service.Service;
import com.jd.journalq.toolkit.service.ServiceThread;
import com.jd.journalq.toolkit.time.SystemClock;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 并行的消息消费方式
 * <br>
 * Created by chengzhiliang on 2018/8/16.
 */
class ConcurrentConsumption extends Service {

    private final Logger logger = LoggerFactory.getLogger(ConcurrentConsumption.class);

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
    // K=分区段,V=过期时间（毫秒）; 用于并行消费应答时对比
    private ConcurrentMap<PartitionSegment, Long> segmentConsumeMap = new ConcurrentHashMap<>();
    // 过期未应答分区段队列，轮询未应答的分区段，将过期的分区分段放入此队列，下次获取的时候，有则从此对获取消息
    // K=分区,V=过期未应答分区段队列
    private ConcurrentMap<ConsumePartition, ConcurrentLinkedQueue<PartitionSegment>> expireQueueMap = new ConcurrentHashMap<>(1000);
    // 消费者：分区段数量；用于控制一个消费者拉取过多分区段
    private ConcurrentMap<String, AtomicInteger> consumerSegmentNumMap = new ConcurrentHashMap<>();
    // 后台线程，见过期未应答的分区段，放入过期队列中
    private Thread thread;
    // K=消费分区，V=消费分区段集合
    private ConcurrentMap<ConsumePartition, List<Position>> concurrentConsumeCache = new ConcurrentHashMap<>();
    // 消费分区锁
    private PartitionLockInstance lockInstance = new PartitionLockInstance();
    // 延迟处理器
    private DelayHandler delayHandler = new DelayHandler();
    // 消费归档服务
    private ArchiveManager archiveManager;

    public ConcurrentConsumption(ClusterManager clusterManager, StoreService storeService, PartitionManager partitionManager,
                                 MessageRetry messageRetry, PositionManager positionManager,
                                 FilterMessageSupport filterMessageSupport, ArchiveManager archiveManager) {
        this.clusterManager = clusterManager;
        this.storeService = storeService;
        this.partitionManager = partitionManager;
        this.messageRetry = messageRetry;
        this.positionManager = positionManager;
        this.filterMessageSupport = filterMessageSupport;
        this.archiveManager = archiveManager;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        thread = new Thread(new ServiceThread(this) {
            @Override
            protected void execute() throws Exception {
                moveSegment2ExpireQueue();
            }

            @Override
            public long getInterval() {
                return 1000 * 5;
            }

            @Override
            public boolean onException(Throwable e) {
                logger.error(e.getMessage(), e);
                return true;
            }

        }, "JMQ_SERVER_CONCURRENT_CONSUMPTION");
        thread.start();
        logger.info("ConcurrentConsumption is started.");
    }

    @Override
    protected void doStop() {
        if (thread != null) {
            thread.interrupt();
        }
        super.doStop();
        logger.info("ConcurrentConsumption is stopped.");
    }

    /**
     * 读取消息
     * <br>
     * 首先尝试从过期未应答队列读取分区段消费
     * <br>
     * 再消费重试消息
     * <br>
     * 接着消费高优先级消息
     * <br>
     * 最后尝试从分区读
     *
     * @param consumer    消费者信息
     * @param count       消息条数
     * @param accessTimes 访问次数用于均匀读取每个分区
     * @return 读取的消息
     */
    protected PullResult getMessage(Consumer consumer, int count, long ackTimeout, long accessTimes) throws JMQException {
        // 首先尝试从过期未应答队列获取分区段进行消费
        PartitionSegment partitionSegment = pollPartitionSegment(consumer);
        PullResult pullResult = new PullResult(consumer, (short) -1, new ArrayList<>(0));
        if (partitionSegment != null) {
            // 尝试从过期未应答队列读
            pullResult = getFromExpireAckQueue(consumer, partitionSegment, accessTimes);
        }
        if (pullResult.getBuffers().size() < 1 && partitionManager.isRetry(consumer)) {
            // 消费待重试的消息
            List<RetryMessageModel> retryMessage = messageRetry.getRetry(consumer.getTopic(), consumer.getApp(), (short) count, 0);
            List<ByteBuffer> messages = convert(retryMessage);
            pullResult.setBuffers(messages);
            if (messages.size() > 0) {
                // 读到重试消息，增加下次读到重试队列的概率（优先处理重试消息）
                partitionManager.increaseRetryProbability(consumer);
            } else {
                // 读不到重试消息，则降低下次读重试队列的概率
                partitionManager.decreaseRetryProbability(consumer);
            }
        }
        List<Short> priorityPartitionList = partitionManager.getPriorityPartition(TopicName.parse(consumer.getTopic()));
        if (pullResult.getBuffers().size() < 1 && priorityPartitionList.size() > 0) {
            // 高优先级分区消费
            pullResult = getFromPartition(consumer, priorityPartitionList, count, ackTimeout, accessTimes);
        }
        if (pullResult.getBuffers().size() < 1) {
            // 消费普通分区消息
            List<Short> partitionList = clusterManager.getMasterPartitionList(TopicName.parse(consumer.getTopic()));
            pullResult = getFromPartition(consumer, partitionList, count, ackTimeout, accessTimes);
        }

        return pullResult;
    }

    /**
     * broker内部应答的APP名称
     */
    private final String innerAppPrefix = "innerFilter@";

    /**
     * 内部应答
     *
     * @param consumer    消费者
     * @param inValidList 无效消息集合
     * @throws JMQException
     */
    private void innerAcknowledge(Consumer consumer, List<ByteBuffer> inValidList) throws JMQException {
        if (inValidList == null) {
            return;
        }
        MessageLocation[] messageLocations = convertMessageLocation(consumer.getTopic(), inValidList);
        acknowledge(messageLocations, consumer, true);
        archiveIfnecessary(messageLocations);
    }

    private void archiveIfnecessary(MessageLocation[] messageLocations) throws JMQException{
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
     * @param topic
     * @param inValidList 有效消息集合
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
     * @param retryMessage
     * @return
     */
    private List<ByteBuffer> convert(List<RetryMessageModel> retryMessage) throws JMQException {
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
                throw new JMQException(JMQCode.SE_IO_ERROR, e);
            }
        }

        return rst;
    }

    /**
     * 从过期为应答队列读取分区段
     *
     * @param consumer         消费者信息
     * @param partitionSegment 分区段
     * @param ackTimeout       应答超时
     * @return 拉取消息对象
     */
    private PullResult getFromExpireAckQueue(Consumer consumer, PartitionSegment partitionSegment, long ackTimeout) throws JMQException {
        short partition = partitionSegment.getPartition();
        int segmentCount = (int) (partitionSegment.getEndIndex() - partitionSegment.getStartIndex());
        long index = partitionSegment.getStartIndex();
        PullResult pullResult = readMessages(consumer, partition, index, segmentCount);
        int msgCount = pullResult.getBuffers().size();
        if (msgCount > 0) {
            // 记录并行消费分区段，用于应答比对
            trackConsumeDetail(consumer, partition, partitionSegment.getStartIndex(), partitionSegment.getEndIndex(), ackTimeout);
        }
        return null;
    }


    /**
     * 从本地磁盘分区消费消息
     *
     * @param consumer      消费者信息
     * @param partitionList 分区集合
     * @param count         消息条数
     * @param ackTimeout    应答超时
     * @param accessTimes   访问次数
     * @return 拉取消息对象
     */
    private PullResult getFromPartition(Consumer consumer, List<Short> partitionList, int count, long ackTimeout, long accessTimes) throws JMQException {
        int partitionSize = partitionList.size();
        int listIndex = -1;
        PullResult pullResult = null;
        for (int i = 0; i < partitionSize; i++) {
            listIndex = partitionManager.selectPartitionIndex(partitionSize, listIndex + i, accessTimes);
            short partition = partitionList.get(listIndex);
            // 同步进行分区操作
            synchronized (lockInstance.getLockInstance(consumer.getTopic(), consumer.getApp(), partition)) {
                // 获取消息拉取位置
                long pullIndex = getPullIndex(consumer, partition);
                logger.debug("get pull index:{}, topic:{}, app:{}, partition:{}", pullIndex, consumer.getTopic(), consumer.getApp(), partition);
                // 读取消息结果
                pullResult = readMessages(consumer, partition, pullIndex, count);
                int msgCount = pullResult.getBuffers().size();
                if (msgCount > 0) {
                    // 更新最新拉取位置，即下次开始拉取的序号
                    long newPullIndex = pullIndex + msgCount;
                    logger.debug("set new pull index:{}, topic:{}, app:{}, partition:{}", newPullIndex, consumer.getTopic(), consumer.getApp(), partition);
                    positionManager.updateLastMsgPullIndex(TopicName.parse(consumer.getTopic()), consumer.getApp(), partition, newPullIndex);
                    // 本次拉取消息列表的结束序号
                    long endIndex = newPullIndex - 1;
                    // 记录并行消费分区段，用于应答比对
                    trackConsumeDetail(consumer, partition, pullIndex, endIndex, ackTimeout);
                    // 退出循环
                    break;
                }
            }
        }
        return pullResult;
    }

    /**
     * 尝试从过期未应答队列中获取分区段进行消费
     *
     * @param consumer 消费者
     * @return 过期未应答的分区段
     */
    private PartitionSegment pollPartitionSegment(Consumer consumer) {
        ConsumePartition consumePartition = new ConsumePartition(consumer.getTopic(), consumer.getApp());
        ConcurrentLinkedQueue<PartitionSegment> partitionSegmentQueue = expireQueueMap.get(consumePartition);
        if (partitionSegmentQueue != null) {
            return partitionSegmentQueue.poll();
        }
        return null;
    }

    /**
     * 进程重启后更新消息拉取位置
     *
     * @param consumer  消费者
     * @param partition 分区
     * @return 消息序号
     */
    private long getPullIndex(Consumer consumer, short partition) throws JMQException {
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
    private PullResult readMessages(Consumer consumer, short partition, long index, int count) throws JMQException {
        // 初始化默认
        PullResult pullResult = new PullResult(consumer, (short) -1, new ArrayList<>(0));
        try {
            int partitionGroup = clusterManager.getPartitionGroupId(TopicName.parse(consumer.getTopic()), partition);
            PartitionGroupStore store = storeService.getStore(consumer.getTopic(), partitionGroup);
            ReadResult readRst = store.read(partition, index, count, Long.MAX_VALUE);
            if (readRst.getCode() == JMQCode.SUCCESS) {
                List<ByteBuffer> byteBufferList = Lists.newArrayList(readRst.getMessages());
                com.jd.journalq.domain.Consumer consumerConfig = clusterManager.getConsumer(TopicName.parse(consumer.getTopic()), consumer.getApp());
                // 过滤消息
                List<ByteBuffer> byteBuffers = filterMessageSupport.filter(consumerConfig, byteBufferList, new FilterCallbackImpl(consumer));

                // 开启延迟消费，过滤未到消费时间的消息
                byteBuffers = delayHandler.handle(consumerConfig.getConsumerPolicy(), byteBuffers);
                // 构建拉取结果
                pullResult = new PullResult(consumer, partition, byteBuffers);
            } else {
                logger.error("read message error, error code[{}]", readRst.getCode());
            }
        } catch (IndexOutOfBoundsException iex) {
            logger.debug("IndexOutOfBoundsException,topic:{},partition:{}", consumer.getTopic(), partition);
        } catch (IOException ioe) {
            throw new JMQException(JMQCode.SE_IO_ERROR, ioe);
        } catch (Exception e) {
            if (e instanceof PositionOverflowException) {
                pullResult.setJmqCode(JMQCode.SE_INDEX_OVERFLOW);
            } else if (e instanceof PositionUnderflowException) {
                pullResult.setJmqCode(JMQCode.SE_INDEX_UNDERFLOW);
            } else {
                logger.error("get message error, consumer: {}, partition: {}", consumer, partition, e);
            }
        }

        return pullResult;
    }

    class FilterCallbackImpl implements FilterCallback {

        private Consumer consumer;

        public FilterCallbackImpl(Consumer consumer) {
            this.consumer = consumer;
        }

        @Override
        public void callback(List<ByteBuffer> byteBuffers) throws JMQException {
            innerAcknowledge(consumer, byteBuffers);
        }
    }

    /**
     * 记录并行消费分区段，用于应答比对
     *
     * @param consumer   消费者
     * @param partition  分区
     * @param startIndex 开始序号
     * @param endIndex   结束序号
     * @param ackTimeOut 应答超时时间
     */
    private void trackConsumeDetail(Consumer consumer, short partition, long startIndex, long endIndex, long ackTimeOut) {
        String topic = consumer.getTopic(), app = consumer.getApp(), clientId = consumer.getId();
        PartitionSegment partitionSegment = new PartitionSegment(topic, app, partition, startIndex, endIndex);
        long expire = ackTimeOut + SystemClock.now();
        segmentConsumeMap.put(partitionSegment, expire);

        AtomicInteger occupyCounter = consumerSegmentNumMap.get(clientId);
        if (occupyCounter == null) {
            occupyCounter = new AtomicInteger(0);
            consumerSegmentNumMap.put(clientId, occupyCounter);
        }
        // 增加计数
        occupyCounter.incrementAndGet();
    }

    /**
     * 消息消费应答
     *
     * @param locations    应答位置信息
     * @param consumer     消费者
     * @param isSuccessAck 是否成功再应答
     * @return
     * @throws JMQException
     */
    public boolean acknowledge(MessageLocation[] locations, Consumer consumer, boolean isSuccessAck) throws JMQException {
        boolean isSuccess = false;
        if (locations.length < 1) {
            return isSuccess;
        }
        String topic = consumer.getTopic();
        String app = consumer.getApp();
        short partition = locations[0].getPartition();
        // 重试应答
        if (partition == Partition.RETRY_PARTITION_ID) {
            return retryAck(topic, app, locations, isSuccessAck);
        }
        // 连续顺序
        long[] indexArr = AcknowledgeSupport.sortMsgLocation(locations);
        if (indexArr != null) {
            PartitionSegment partitionSegment = new PartitionSegment(topic, app, partition, indexArr[0], indexArr[1]);
            ConsumePartition consumePartition = new ConsumePartition(topic, app, partition);
            if (segmentConsumeMap.containsKey(partitionSegment) || isExpireQueueContains(consumePartition, partitionSegment)) {
                // 尝试更新应答位置
                tryUpdateAckPosition(consumePartition, indexArr);
                // 从分区段消费记录中移除
                segmentConsumeMap.remove(partitionSegment);
                // 从过期未应答队列中移除
                removeFromExpireQueue(consumePartition, partitionSegment);
                // 设置应答成功
                isSuccess = true;
            }
        }

        return isSuccess;
    }

    /**
     * 重试应答
     *
     * @param topic
     * @param app
     * @param isSuccess
     * @return
     * @throws JMQException
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
        } catch (JMQException e) {
            logger.error("RetryAck error.", e);
            return false;
        }
        return true;
    }

    /**
     * 过期队列中是否包含该分区段
     *
     * @param consumePartition 消费分区
     * @param partitionSegment 分区段
     * @return 是否包含该分区段
     */
    private boolean isExpireQueueContains(ConsumePartition consumePartition, PartitionSegment partitionSegment) {
        boolean isContains = false;
        ConcurrentLinkedQueue<PartitionSegment> queue = expireQueueMap.get(consumePartition);
        if (queue != null && partitionSegment != null) {
            isContains = queue.contains(partitionSegment);
        }
        return isContains;
    }

    /**
     * 从过期未应答的队列删除分区段
     *
     * @param consumePartition 消费分区
     * @param partitionSegment 分区段
     */
    private void removeFromExpireQueue(ConsumePartition consumePartition, PartitionSegment partitionSegment) {
        ConcurrentLinkedQueue<PartitionSegment> queue = expireQueueMap.get(consumePartition);
        if (queue != null && partitionSegment != null) {
            queue.remove(partitionSegment);
        }
    }

    /**
     * 添加过期未应答的分区段到过期队列
     *
     * @param consumePartition 消费分区
     * @param partitionSegment 分区段
     */
    private void addToExpireQueue(ConsumePartition consumePartition, PartitionSegment partitionSegment) {
        ConcurrentLinkedQueue<PartitionSegment> queue = expireQueueMap.get(consumePartition);
        if (partitionSegment != null) {
            queue = new ConcurrentLinkedQueue<>();
            expireQueueMap.putIfAbsent(consumePartition, queue);
        }
        queue.add(partitionSegment);
    }

    /**
     * 将过期未应答的分区段移动到过期队列
     */
    private void moveSegment2ExpireQueue() {
        Iterator<PartitionSegment> iterator = segmentConsumeMap.keySet().iterator();
        long now = SystemClock.now();
        while (iterator.hasNext()) {
            PartitionSegment next = iterator.next();
            Long expireTime = segmentConsumeMap.get(next);
            if (expireTime >= now) {
                segmentConsumeMap.remove(next);
                addToExpireQueue(new ConsumePartition(next.getTopic(), next.getApp(), next.getPartition()), next);
            }
        }
    }

    /**
     * 尝试修改消费应答位置
     * 多线程同步更新同一个分区的消费位置
     *
     * @param consumePartition 分区消费
     * @param indexArr         应答分区段的开始序号和结束序号
     */
    private void tryUpdateAckPosition(ConsumePartition consumePartition, long[] indexArr) throws JMQException {
        synchronized (lockInstance.getLockInstance(consumePartition)) {
            String topic = consumePartition.getTopic();
            String app = consumePartition.getApp();
            short partition = consumePartition.getPartition();
            // 添加到该分区的应答分区段集合
            addAckSegment(consumePartition, indexArr[0], indexArr[1]);
            // 排序、合并，并返回第一个分区段
            Position position = sortAndMerge(consumePartition);
            // 查询应答位置
            long lastMsgAckIndex = positionManager.getLastMsgAckIndex(TopicName.parse(topic), app, partition);
            if (lastMsgAckIndex == position.getAckStartIndex()) {
                // 将当前序号向后移动一位
                long updateMsgAckIndex = position.getAckCurIndex() + 1;
                positionManager.updateLastMsgAckIndex(TopicName.parse(topic), app, partition, updateMsgAckIndex);
            }
        }
    }

    /**
     * 添加应答的分区段
     *
     * @param consumePartition
     * @param ackStartIndex
     * @param ackCurIndex
     */
    private void addAckSegment(ConsumePartition consumePartition, long ackStartIndex, long ackCurIndex) {
        List<Position> positionList = concurrentConsumeCache.get(consumePartition);
        if (positionList == null) {
            positionList = new ArrayList<>();
            concurrentConsumeCache.put(consumePartition, positionList);
        }
        Position position = new Position(ackStartIndex, ackCurIndex, -1, -1);
        positionList.add(position);
    }


    /**
     * 按照消费应答开始序号将分段段进行排序,并返回第一分区段的位置
     *
     * @param consumePartition 消费分区
     * @return 应答位置
     */
    private Position sortAndMerge(ConsumePartition consumePartition) {
        List<Position> positionList = concurrentConsumeCache.get(consumePartition);
        if (positionList.size() == 0) {
            return null;
        }
        // 将分区段按照拉取消息的起始序号进行排序
        List<Position> sortPositionList = sortByAckStartIndex(positionList);
        // 将连续相邻的分区段合并
        List<Position> mergePositionList = mergeSequenceSegment(sortPositionList);
        concurrentConsumeCache.put(consumePartition, mergePositionList);
        return mergePositionList.get(0);
    }

    /**
     * 按照消费应答开始序号将分段段进行排序
     *
     * @param list
     * @return
     */
    private List<Position> sortByAckStartIndex(List<Position> list) {
        return list.stream().sorted((thisPosition, thatPosition) -> (int) (thisPosition.getAckStartIndex() - thatPosition.getAckStartIndex())).collect(Collectors.toList());
    }

    /**
     * 合并相邻的消费区间
     * <br>
     * 判断包含关系
     * <br>
     * 判断连续关系
     *
     * @param sortPositionList 已经排序的消费位置列表
     * @return
     */
    private List<Position> mergeSequenceSegment(List<Position> sortPositionList) {
        if (sortPositionList.size() <= 1) {
            return sortPositionList;
        }
        List<Position> mergeList = new ArrayList<>();
        // 初始化两个待比较的值
        Position last = null;
        Position next = null;
        int i = 0;
        int size = sortPositionList.size();
        while (i < size) {
            if (i == 0) {
                last = sortPositionList.get(i);
                next = sortPositionList.get(i += 1);
            }
            Position newPosition = tryMergeTwoSegment(last, next);
            if (newPosition == null) {
                mergeList.add(last);
                last = next;
            } else {
                last = newPosition;
            }
            if (i + 1 < size) {
                next = sortPositionList.get(i + 1);
            } else {
                mergeList.add(last);
            }
            i++;
        }

        return mergeList;
    }

    /**
     * 尝试合并两个消费区间
     * <br>
     * 判断包含关系
     * <br>
     * 判断连续关系
     *
     * @param last
     * @param next
     * @return
     */
    private Position tryMergeTwoSegment(Position last, Position next) {
        // 包含关系
        if (last.getPullCurIndex() >= next.getPullCurIndex()) {
            return last;
        }
        // 连续关系(两个连续的序号相差1)
        if (last.getAckStartIndex() + 1 == next.getAckCurIndex()) {
            // 这里只关心应答序号，消费序号都用-1标示，稍后再合并消费位置的会填充。
            return new Position(last.getAckStartIndex(), next.getAckCurIndex(), -1, -1);
        }
        // 否则返回空
        return null;
    }


    /**
     * 分割的一小段分区，用于并行消费
     */
    private class PartitionSegment {
        // 主题
        private String topic;
        // 应用
        private String app;
        // 分区
        private short partition;
        // 开始序号
        private long startIndex;
        // 结束序号
        private long endIndex;

        public PartitionSegment(String topic, String app, short partition, long startIndex, long endIndex) {
            this.topic = topic;
            this.app = app;
            this.partition = partition;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public String getApp() {
            return app;
        }

        public void setApp(String app) {
            this.app = app;
        }

        public short getPartition() {
            return partition;
        }

        public void setPartition(short partition) {
            this.partition = partition;
        }

        public long getStartIndex() {
            return startIndex;
        }

        public void setStartIndex(long startIndex) {
            this.startIndex = startIndex;
        }

        public long getEndIndex() {
            return endIndex;
        }

        public void setEndIndex(long endIndex) {
            this.endIndex = endIndex;
        }

        @Override
        public int hashCode() {
            int result = topic.hashCode();
            result = 31 * result + app.hashCode();
            result = 31 * result + Short.hashCode(partition);
            result = 31 * result + Long.hashCode(startIndex);
            result = 31 * result + Long.hashCode(endIndex);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            PartitionSegment that = (PartitionSegment) obj;
            if (!StringUtils.equals(this.topic, that.topic)) {
                return false;
            }
            if (!StringUtils.equals(this.app, that.app)) {
                return false;
            }
            if (this.partition != that.partition) {
                return false;
            }
            if (this.startIndex != that.startIndex) {
                return false;
            }
            if (this.endIndex != that.endIndex) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("PartitionSegment{");
            sb.append("topic='").append(topic).append('\'');
            sb.append(", app=").append(app);
            sb.append(", partition=").append(partition);
            sb.append(", startIndex=").append(startIndex);
            sb.append(", endIndex=").append(endIndex);
            sb.append('}');
            return sb.toString();
        }
    }

}
