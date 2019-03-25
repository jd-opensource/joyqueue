package com.jd.journalq.broker.kafka.network.codec;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.command.ProduceRequest;
import com.jd.journalq.broker.kafka.command.ProduceResponse;
import com.jd.journalq.broker.kafka.message.KafkaBrokerMessage;
import com.jd.journalq.broker.kafka.message.KafkaMessageSerializer;
import com.jd.journalq.broker.kafka.model.ProducePartitionStatus;
import com.jd.journalq.broker.kafka.network.KafkaHeader;
import com.jd.journalq.broker.kafka.network.KafkaPayloadCodec;
import com.jd.journalq.common.domain.TopicName;
import com.jd.journalq.common.network.serializer.Serializer;
import com.jd.journalq.common.network.transport.command.Type;
import com.jd.journalq.common.network.transport.exception.TransportException;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

/**
 * ProduceCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/5
 */
public class ProduceCodec implements KafkaPayloadCodec<ProduceResponse>, Type {

    @Override
    public Object decode(KafkaHeader header, ByteBuf buffer) throws Exception {
        ProduceRequest produceRequest = new ProduceRequest();
        Table<TopicName, Integer, List<KafkaBrokerMessage>> topicPartitionMessages = HashBasedTable.create();

        if (header.getVersion() >= 3) {
            produceRequest.setTransactionalId(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        }

        produceRequest.setRequiredAcks(buffer.readShort());
        produceRequest.setAckTimeoutMs(buffer.readInt());
        int topicCount = buffer.readInt();
        for (int i = 0; i < topicCount; i++) {
            TopicName topic = TopicName.parse(Serializer.readString(buffer, Serializer.SHORT_SIZE));
            int partitionCount = buffer.readInt();
            for (int j = 0; j < partitionCount; j++) {
                int partition = buffer.readInt();
                int messageSetSize = buffer.readInt();

                byte[] bytes = new byte[messageSetSize];
                buffer.readBytes(bytes);

                // TODO buffer优化，不需要buffer
                List<KafkaBrokerMessage> messages = KafkaMessageSerializer.readMessages(ByteBuffer.wrap(bytes));
                topicPartitionMessages.put(topic, partition, messages);
            }
        }

        produceRequest.setTopicPartitionMessages(topicPartitionMessages);
        return produceRequest;
    }

    @Override
    public void encode(ProduceResponse payload, ByteBuf buffer) throws Exception {
        int version = payload.getVersion();
        buffer.writeInt(payload.getProducerResponseStatuss().size());
        for (Map.Entry<String, List<ProducePartitionStatus>> entry : payload.getProducerResponseStatuss().entrySet()) {
            try {
                Serializer.write(entry.getKey(), buffer, Serializer.SHORT_SIZE);
            } catch (Exception e) {
                throw new TransportException.CodecException(e);
            }
            buffer.writeInt(entry.getValue().size());
            for (ProducePartitionStatus partitionStatus : entry.getValue()) {
                buffer.writeInt(partitionStatus.getPartition());
                buffer.writeShort(partitionStatus.getErrorCode());
                buffer.writeLong(partitionStatus.getOffset());
                if (version >= 2) {
                    // TODO: log_append_time
                    buffer.writeLong(-1);
                }
                if (version >= 5) {
                    // TODO: log_start_offset
                    buffer.writeLong(0);
                }
            }
        }
        if (version >= 1) {
            // throttle_time_ms
            buffer.writeInt(payload.getThrottleTimeMs());
        }
    }

    @Override
    public int type() {
        return KafkaCommandType.PRODUCE.getCode();
    }
}