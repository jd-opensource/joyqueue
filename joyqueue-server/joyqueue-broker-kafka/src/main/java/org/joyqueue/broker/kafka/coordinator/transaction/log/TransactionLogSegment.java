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
package org.joyqueue.broker.kafka.coordinator.transaction.log;

import com.google.common.collect.Lists;
import org.joyqueue.broker.buffer.Serializer;
import org.joyqueue.broker.consumer.Consume;
import org.joyqueue.broker.consumer.model.PullResult;
import org.joyqueue.broker.kafka.config.KafkaConfig;
import org.joyqueue.broker.kafka.coordinator.transaction.domain.TransactionDomain;
import org.joyqueue.broker.kafka.coordinator.transaction.helper.TransactionSerializer;
import org.joyqueue.broker.producer.Produce;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.message.BrokerMessage;
import org.joyqueue.network.session.Consumer;
import org.joyqueue.network.session.Producer;
import org.joyqueue.toolkit.network.IpUtil;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

/**
 * TransactionLogSegment
 *
 * author: gaohaoxiang
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
        if (!pullResult.getCode().equals(JoyQueueCode.SUCCESS)) {
            logger.error("read transaction log exception, partition: {}, index: {}, count: {}", partition, index, count, pullResult.getCode());
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
        return message;
    }

    public short getPartition() {
        return partition;
    }
}