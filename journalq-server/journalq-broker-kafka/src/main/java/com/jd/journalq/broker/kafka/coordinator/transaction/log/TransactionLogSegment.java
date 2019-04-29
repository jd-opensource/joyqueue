package com.jd.journalq.broker.kafka.coordinator.transaction.log;

import com.google.common.collect.Lists;
import com.jd.journalq.broker.buffer.Serializer;
import com.jd.journalq.broker.consumer.Consume;
import com.jd.journalq.broker.consumer.model.PullResult;
import com.jd.journalq.broker.kafka.config.KafkaConfig;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionDomain;
import com.jd.journalq.broker.kafka.coordinator.transaction.helper.TransactionSerializer;
import com.jd.journalq.broker.producer.Produce;
import com.jd.journalq.exception.JournalqCode;
import com.jd.journalq.message.BrokerMessage;
import com.jd.journalq.network.session.Consumer;
import com.jd.journalq.network.session.Producer;
import com.jd.journalq.toolkit.network.IpUtil;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.zip.CRC32;

/**
 * TransactionLogSegment
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/25
 */
public class TransactionLogSegment {

    protected static final Logger logger = LoggerFactory.getLogger(TransactionLogSegment.class);

    private final byte[] LOCAL_IP = IpUtil.getLocalIp().getBytes();

    private KafkaConfig config;
    private String topic;
    private short partition;
    private Produce produce;
    private Consume consume;
    private Consumer consumer;
    private Producer producer;

    public TransactionLogSegment(KafkaConfig config, String topic, short partition, Produce produce, Consume consume, Producer producer, Consumer consumer) {
        this.config = config;
        this.topic = topic;
        this.partition = partition;
        this.produce = produce;
        this.consume = consume;
        this.producer = producer;
        this.consumer = consumer;
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

    public boolean batchWrite(String app, String transactionId, List<byte[]> bodyList) throws Exception {
        List<BrokerMessage> messages = Lists.newArrayListWithCapacity(bodyList.size());
        for (byte[] body : bodyList) {
            BrokerMessage message = convertMessage(body);
            messages.add(message);
        }
        produce.putMessage(producer, messages, config.getTransactionLogWriteQosLevel(), config.getTransactionSyncTimeout());
        return true;
    }

    public boolean write(String app, String transactionId, byte[] body) throws Exception {
        BrokerMessage message = convertMessage(body);
        produce.putMessage(producer, Lists.newArrayList(message), config.getTransactionLogWriteQosLevel(), config.getTransactionSyncTimeout());
        return true;
    }

    protected BrokerMessage convertMessage(byte[] body) {
        BrokerMessage message = new BrokerMessage();
        message.setTopic(topic);
        message.setApp(producer.getApp());
        message.setBody(body);
        message.setClientIp(LOCAL_IP);
        message.setPartition(partition);

        CRC32 crc32 = new CRC32();
        crc32.update(body);
        message.setBodyCRC(crc32.getValue());
        return message;
    }

    public short getPartition() {
        return partition;
    }
}