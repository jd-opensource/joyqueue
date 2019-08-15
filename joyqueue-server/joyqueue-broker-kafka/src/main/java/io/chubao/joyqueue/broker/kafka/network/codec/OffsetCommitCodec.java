package io.chubao.joyqueue.broker.kafka.network.codec;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.chubao.joyqueue.broker.kafka.KafkaCommandType;
import io.chubao.joyqueue.broker.kafka.command.OffsetCommitRequest;
import io.chubao.joyqueue.broker.kafka.command.OffsetCommitResponse;
import io.chubao.joyqueue.broker.kafka.model.OffsetAndMetadata;
import io.chubao.joyqueue.broker.kafka.model.OffsetMetadataAndError;
import io.chubao.joyqueue.broker.kafka.network.KafkaHeader;
import io.chubao.joyqueue.broker.kafka.network.KafkaPayloadCodec;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.Map;

/**
 * OffsetCommitCodec
 *
 * author: gaohaoxiang
 * date: 2018/11/5
 */
public class OffsetCommitCodec implements KafkaPayloadCodec<OffsetCommitResponse>, Type {

    @Override
    public Object decode(KafkaHeader header, ByteBuf buffer) throws Exception {
        OffsetCommitRequest offsetCommitRequest = new OffsetCommitRequest();

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
        Map<String, List<OffsetAndMetadata>> offsets = Maps.newHashMapWithExpectedSize(topicCount);

        for (int i = 0; i < topicCount; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            int partitionCount = buffer.readInt();
            List<OffsetAndMetadata> offsetList = Lists.newArrayListWithCapacity(partitionCount);
            offsets.put(topic, offsetList);

            for (int j = 0; j < partitionCount; j++) {
                int partition = buffer.readInt();
                long offset = buffer.readLong();
                long timestamp = header.getVersion() == 1 ? buffer.readLong() : OffsetCommitRequest.DEFAULT_TIMESTAMP;
                String metadata = Serializer.readString(buffer, Serializer.SHORT_SIZE);
                OffsetAndMetadata offsetAndMetadata = new OffsetAndMetadata(partition, offset, metadata, timestamp);

                offsetList.add(offsetAndMetadata);
            }
        }

        offsetCommitRequest.setOffsets(offsets);
        return offsetCommitRequest;
    }

    @Override
    public void encode(OffsetCommitResponse payload, ByteBuf buffer) throws Exception {
        if (payload.getVersion() >= 3) {
            // throttle_time_ms
            buffer.writeInt(payload.getThrottleTimeMs());
        }

        buffer.writeInt(payload.getOffsets().size());
        for (Map.Entry<String, List<OffsetMetadataAndError>> entry : payload.getOffsets().entrySet()) {
            Serializer.write(entry.getKey(), buffer, Serializer.SHORT_SIZE);
            buffer.writeInt(entry.getValue().size());
            for (OffsetMetadataAndError offsetMetadataAndError : entry.getValue()) {
                buffer.writeInt(offsetMetadataAndError.getPartition());
                buffer.writeShort(offsetMetadataAndError.getError());
            }
        }
    }

    @Override
    public int type() {
        return KafkaCommandType.OFFSET_COMMIT.getCode();
    }
}