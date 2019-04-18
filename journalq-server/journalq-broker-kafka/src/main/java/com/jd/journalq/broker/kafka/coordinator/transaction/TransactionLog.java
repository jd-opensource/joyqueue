package com.jd.journalq.broker.kafka.coordinator.transaction;

import com.google.common.collect.Lists;
import com.jd.journalq.broker.consumer.Consume;
import com.jd.journalq.broker.kafka.config.KafkaConfig;
import com.jd.journalq.broker.kafka.coordinator.Coordinator;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionMarker;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionOffset;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionPrepare;
import com.jd.journalq.broker.producer.Produce;
import com.jd.journalq.broker.producer.PutResult;
import com.jd.journalq.domain.PartitionGroup;
import com.jd.journalq.message.BrokerMessage;
import com.jd.journalq.network.session.Consumer;
import com.jd.journalq.network.session.Producer;
import com.jd.journalq.toolkit.network.IpUtil;
import com.jd.journalq.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
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
public class TransactionLog extends Service {

    private static final String TRANSACTION_APP = "_TRANSACTION_LOG_";

    protected static final Logger logger = LoggerFactory.getLogger(TransactionLog.class);

    private KafkaConfig config;
    private Produce produce;
    private Consume consume;
    private Coordinator coordinator;

    private final Consumer consumer;
    private final Producer producer;

    public TransactionLog(KafkaConfig config, Produce produce, Consume consume, Coordinator coordinator) {
        this.config = config;
        this.produce = produce;
        this.consume = consume;
        this.coordinator = coordinator;
        this.consumer = initConsumer();
        this.producer = initProducer();
    }

    protected Consumer initConsumer() {
        return new Consumer(TRANSACTION_APP, coordinator.getTransactionTopic().getFullName(), TRANSACTION_APP, Consumer.ConsumeType.JMQ);
    }

    protected Producer initProducer() {
        return new Producer(TRANSACTION_APP, coordinator.getTransactionTopic().getFullName(), TRANSACTION_APP, Producer.ProducerType.JMQ);
    }

    public boolean writeCommitOffsets(String app, String transactionId, Map<String, List<TransactionOffset>> partitions) throws Exception {
        byte[] body = TransactionSerializer.serializeOffsets(partitions);
        return write(app, transactionId, body);
    }

    public boolean writePrepare(String app, String transactionId, List<TransactionPrepare> prepareList) throws Exception {
        List<byte[]> bodyList = Lists.newArrayListWithCapacity(prepareList.size());
        for (TransactionPrepare prepare : prepareList) {
            byte[] body = TransactionSerializer.serializePrepare(prepare);
            bodyList.add(body);
        }
        return batchWrite(app, transactionId, bodyList);
    }

    public boolean writeMarker(TransactionMarker marker) throws Exception {
        byte[] body = TransactionSerializer.serializeMarker(marker);
        return write(marker.getApp(), marker.getTransactionId(), body);
    }

    protected List<byte[]> read(short partition, long index, int count) throws Exception {
        return null;
    }

    protected boolean batchWrite(String app, String transactionId, List<byte[]> bodyList) throws Exception {
        List<BrokerMessage> messages = Lists.newArrayListWithCapacity(bodyList.size());
        short partittion = resolvePartition(app, transactionId);
        for (byte[] body : bodyList) {
            BrokerMessage message = new BrokerMessage();
            message.setTopic(coordinator.getTransactionTopic().getFullName());
            message.setApp(initProducer().getApp());
            message.setBody(body);
            message.setClientIp(IpUtil.getLocalIp().getBytes());
            message.setPartition(partittion);
            messages.add(message);
        }
        PutResult putResult = produce.putMessage(producer, messages, config.getTransactionLogWriteQosLevel(), config.getTransactionSyncTimeout());
        return true;
    }

    protected boolean write(String app, String transactionId, byte[] body) throws Exception {
        short partittion = resolvePartition(app, transactionId);
        BrokerMessage message = new BrokerMessage();
        message.setTopic(coordinator.getTransactionTopic().getFullName());
        message.setApp(initProducer().getApp());
        message.setBody(body);
        message.setClientIp(IpUtil.getLocalIp().getBytes());
        message.setPartition(partittion);
        PutResult putResult = produce.putMessage(producer, Lists.newArrayList(message), config.getTransactionLogWriteQosLevel(), config.getTransactionSyncTimeout());
        return true;
    }

    protected short resolvePartition(String app, String transactionId) {
        PartitionGroup partitionGroup = coordinator.getTransactionPartitionGroup(app);
        Set<Short> partitions = partitionGroup.getPartitions();
        Iterator<Short> iterator = partitions.iterator();
        if (partitions.size() == 1) {
            return iterator.next().shortValue();
        }
        int index = transactionId.hashCode() % partitions.size();
        for (int i = 0; i < index; i++) {

        }
        return 0;
    }
}