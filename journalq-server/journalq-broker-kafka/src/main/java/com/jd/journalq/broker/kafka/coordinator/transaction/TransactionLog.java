package com.jd.journalq.broker.kafka.coordinator.transaction;

import com.google.common.collect.Lists;
import com.jd.journalq.broker.buffer.Serializer;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.broker.consumer.Consume;
import com.jd.journalq.broker.consumer.model.PullResult;
import com.jd.journalq.broker.kafka.config.KafkaConfig;
import com.jd.journalq.broker.kafka.coordinator.Coordinator;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionDomain;
import com.jd.journalq.broker.producer.Produce;
import com.jd.journalq.broker.producer.PutResult;
import com.jd.journalq.domain.PartitionGroup;
import com.jd.journalq.domain.TopicConfig;
import com.jd.journalq.exception.JournalqCode;
import com.jd.journalq.message.BrokerMessage;
import com.jd.journalq.network.session.Consumer;
import com.jd.journalq.network.session.Producer;
import com.jd.journalq.toolkit.network.IpUtil;
import com.jd.journalq.toolkit.service.Service;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TransactionLog
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/15
 */
// TODO 异常处理
// TODO 补充日志
// TODO 分区处理
public class TransactionLog extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(TransactionLog.class);

    private KafkaConfig config;
    private Produce produce;
    private Consume consume;
    private Coordinator coordinator;
    private ClusterManager clusterManager;

    private Consumer consumer;
    private Producer producer;
    private short partition = 0;

    public TransactionLog(KafkaConfig config, Produce produce, Consume consume, Coordinator coordinator, ClusterManager clusterManager) {
        this.config = config;
        this.produce = produce;
        this.consume = consume;
        this.coordinator = coordinator;
        this.clusterManager = clusterManager;
    }

    @Override
    protected void validate() throws Exception {
        this.consumer = initConsumer();
        this.producer = initProducer();
//        this.partition = resolvePartition();
    }

    protected Consumer initConsumer() {
        return new Consumer(config.getTransactionLogApp(), coordinator.getTransactionTopic().getFullName(), config.getTransactionLogApp(), Consumer.ConsumeType.INTERNAL);
    }

    protected Producer initProducer() {
        return new Producer(config.getTransactionLogApp(), coordinator.getTransactionTopic().getFullName(), config.getTransactionLogApp(), Producer.ProducerType.INTERNAL);
    }

    protected short resolvePartition() {
        TopicConfig topicConfig = coordinator.getTransactionTopicConfig();
        for (Map.Entry<Integer, PartitionGroup> entry : topicConfig.getPartitionGroups().entrySet()) {
            PartitionGroup partitionGroup = entry.getValue();
            if (partitionGroup.getLeaderBroker() == null) {
                continue;
            }
            if (partitionGroup.getLeaderBroker().getId().equals(clusterManager.getBrokerId())) {
                return partitionGroup.getPartitions().iterator().next();
            }
        }
        return -1;
    }

    public boolean write(String app, String transactionId, TransactionDomain transactionDomain) throws Exception {
        byte[] body = TransactionSerializer.serialize(transactionDomain);
        return write(app, transactionId, body);
    }

    public boolean batchWrite(String app, String transactionId, Set<? extends TransactionDomain> transactionDomains) throws Exception {
        List<byte[]> bodyList = Lists.newArrayListWithCapacity(transactionDomains.size());
        for (TransactionDomain transactionDomain : transactionDomains) {
            byte[] body = TransactionSerializer.serialize(transactionDomain);
            bodyList.add(body);
        }
        return batchWrite(app, transactionId, bodyList);
    }

    public long getIndex() {
        long ackIndex = consume.getAckIndex(consumer, partition);
        if (ackIndex < 0) {
            ackIndex = 0;
        }
        return ackIndex;
    }

    public List<TransactionDomain> read(long index, int count) throws Exception {
        List<ByteBuffer> buffers = doRead(partition, index, count);
        if (CollectionUtils.isEmpty(buffers)) {
            return Collections.emptyList();
        }

        List<TransactionDomain> result = Lists.newArrayListWithCapacity(buffers.size());
        for (ByteBuffer buffer : buffers) {
            TransactionDomain transactionDomain = TransactionSerializer.deserialize(buffer);
            result.add(transactionDomain);
        }

        return result;
    }

    public void saveIndex(long index) throws Exception {
        doSaveIndex(partition, index);
    }

    protected void doSaveIndex(short partition, long index) throws Exception {
        consume.setAckIndex(consumer, partition, index);
    }

    protected List<ByteBuffer> doRead(short partition, long index, int count) throws Exception {
        PullResult pullResult = consume.getMessage(consumer, partition, index, count);
        if (!pullResult.getJournalqCode().equals(JournalqCode.SUCCESS)) {
            logger.error("read transaction log exception, partition: {}, index: {}, count: {}", partition, index, count, pullResult.getJournalqCode());
            return Collections.emptyList();
        }
        List<ByteBuffer> buffers = Lists.newArrayListWithCapacity(pullResult.getBuffers().size());
        for (ByteBuffer buffer : pullResult.getBuffers()) {
            BrokerMessage brokerMessage = Serializer.readBrokerMessage(buffer);
            buffers.add(brokerMessage.getBody());
        }
        return buffers;
    }

    protected boolean batchWrite(String app, String transactionId, List<byte[]> bodyList) throws Exception {
        List<BrokerMessage> messages = Lists.newArrayListWithCapacity(bodyList.size());
        for (byte[] body : bodyList) {
            BrokerMessage message = new BrokerMessage();
            message.setTopic(coordinator.getTransactionTopic().getFullName());
            message.setApp(initProducer().getApp());
            message.setBody(body);
            message.setClientIp(IpUtil.getLocalIp().getBytes());
            message.setPartition(partition);
            messages.add(message);
        }
        PutResult putResult = produce.putMessage(producer, messages, config.getTransactionLogWriteQosLevel(), config.getTransactionSyncTimeout());
        return true;
    }

    protected boolean write(String app, String transactionId, byte[] body) throws Exception {
        BrokerMessage message = new BrokerMessage();
        message.setTopic(coordinator.getTransactionTopic().getFullName());
        message.setApp(initProducer().getApp());
        message.setBody(body);
        message.setClientIp(IpUtil.getLocalIp().getBytes());
        message.setPartition(partition);
        PutResult putResult = produce.putMessage(producer, Lists.newArrayList(message), config.getTransactionLogWriteQosLevel(), config.getTransactionSyncTimeout());
        return true;
    }

    protected short resolvePartition(String app, String transactionId) {
        return partition;
    }
}