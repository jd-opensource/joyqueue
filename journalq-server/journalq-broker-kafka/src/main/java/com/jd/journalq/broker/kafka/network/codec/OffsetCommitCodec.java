package com.jd.journalq.broker.kafka.network.codec;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.jd.journalq.broker.kafka.network.KafkaHeader;
import com.jd.journalq.broker.kafka.network.KafkaPayloadCodec;
import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.command.OffsetCommitRequest;
import com.jd.journalq.broker.kafka.command.OffsetCommitResponse;
import com.jd.journalq.broker.kafka.model.OffsetAndMetadata;
import com.jd.journalq.broker.kafka.model.OffsetMetadataAndError;
import com.jd.journalq.common.network.serializer.Serializer;
import com.jd.journalq.common.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.Map;
import java.util.Set;

/**
 * OffsetCommitCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/5
 */
public class OffsetCommitCodec implements KafkaPayloadCodec<OffsetCommitResponse>, Type {

    @Override
    public Object decode(KafkaHeader header, ByteBuf buffer) throws Exception {
        OffsetCommitRequest offsetCommitRequest = new OffsetCommitRequest();
        Table<String, Integer, OffsetAndMetadata> topicPartitionOffsetMetadata = HashBasedTable.create();

        offsetCommitRequest.setGroupId(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        // 0.8 above
        if (header.getVersion() >= 1) {
            offsetCommitRequest.setGroupGenerationId(buffer.readInt());
            offsetCommitRequest.setMemberId(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        } else {
            offsetCommitRequest.setGroupGenerationId(OffsetCommitRequest.DEFAULT_GENERATION_ID);
            offsetCommitRequest.setMemberId(OffsetCommitRequest.DEFAULT_CONSUMER_ID);
        }
        // 0.9 above
        if (header.getVersion() >= 2) {
            offsetCommitRequest.setRetentionTime(buffer.readLong());
        } else {
            offsetCommitRequest.setRetentionTime(OffsetCommitRequest.DEFAULT_TIMESTAMP);
        }

        int topicCount = buffer.readInt();
        for (int i = 0; i < topicCount; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            int partitionCount = buffer.readInt();
            for (int j = 0; j < partitionCount; j++) {
                int partitionId = buffer.readInt();
                long offset = buffer.readLong();
                long timestamp = header.getVersion() == 1 ? buffer.readLong() : OffsetCommitRequest.DEFAULT_TIMESTAMP;
                String metadata = Serializer.readString(buffer, Serializer.SHORT_SIZE);
                OffsetAndMetadata offsetAndMetadata = new OffsetAndMetadata(offset, metadata, timestamp);
                topicPartitionOffsetMetadata.put(topic, partitionId, offsetAndMetadata);
            }
        }

        offsetCommitRequest.setOffsetAndMetadata(topicPartitionOffsetMetadata);
        return offsetCommitRequest;
    }

    @Override
    public void encode(OffsetCommitResponse payload, ByteBuf buffer) throws Exception {
        if (payload.getVersion() >= 3) {
            // throttle_time_ms
            buffer.writeInt(payload.getThrottleTimeMs());
        }

        Table<String, Integer, OffsetMetadataAndError> commitOffsetStatus = payload.getCommitStatus();
        Set<String> topics = commitOffsetStatus.rowKeySet();
        buffer.writeInt(topics.size());
        for (String topic : topics) {
            Serializer.write(topic, buffer, Serializer.SHORT_SIZE);
            Map<Integer, OffsetMetadataAndError> partitionStatusMap = commitOffsetStatus.row(topic);
            Set<Integer> partitions = partitionStatusMap.keySet();
            buffer.writeInt(partitions.size());
            for (int partition : partitions) {
                buffer.writeInt(partition);
                OffsetMetadataAndError status = partitionStatusMap.get(partition);
                buffer.writeShort(status.getError());
            }
        }
    }

    @Override
    public int type() {
        return KafkaCommandType.OFFSET_COMMIT.getCode();
    }
}