package com.jd.journalq.broker.kafka.coordinator.transaction;

import com.google.common.collect.Lists;
import com.jd.journalq.broker.consumer.Consume;
import com.jd.journalq.broker.kafka.config.KafkaConfig;
import com.jd.journalq.broker.kafka.coordinator.Coordinator;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionMarker;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionPrepare;
import com.jd.journalq.broker.producer.Produce;
import com.jd.journalq.broker.producer.PutResult;
import com.jd.journalq.message.BrokerMessage;
import com.jd.journalq.network.session.Consumer;
import com.jd.journalq.network.session.Producer;
import com.jd.journalq.toolkit.network.IpUtil;
import com.jd.journalq.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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

    public boolean writePrepare(TransactionPrepare prepare) throws Exception {
        byte[] body = TransactionSerializer.serializePrepare(prepare);
        return write(prepare.getApp(), prepare.getTransactionId(), body);
    }

    public boolean writeMarker(TransactionMarker marker) throws Exception {
        byte[] body = TransactionSerializer.serializeMarker(marker);
        return write(marker.getApp(), marker.getTransactionId(), body);
    }

    protected List<byte[]> read(short partition, long index, int count) throws Exception {
        return null;
    }

    protected boolean write(String app, String transactionId, byte[] body) throws Exception {
        BrokerMessage message = new BrokerMessage();
        message.setTopic(coordinator.getTransactionTopic().getFullName());
        message.setApp(initProducer().getApp());
        message.setBody(body);
        message.setClientIp(IpUtil.getLocalIp().getBytes());
        PutResult putResult = produce.putMessage(producer, Lists.newArrayList(message), config.getTransactionLogWriteQosLevel(), config.getTransactionSyncTimeout());
        return true;
    }
}