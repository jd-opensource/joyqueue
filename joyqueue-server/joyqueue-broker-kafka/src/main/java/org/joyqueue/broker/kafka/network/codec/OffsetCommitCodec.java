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
package org.joyqueue.broker.kafka.network.codec;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joyqueue.broker.kafka.KafkaCommandType;
import org.joyqueue.broker.kafka.command.OffsetCommitRequest;
import org.joyqueue.broker.kafka.command.OffsetCommitResponse;
import org.joyqueue.broker.kafka.model.OffsetAndMetadata;
import org.joyqueue.broker.kafka.model.OffsetMetadataAndError;
import org.joyqueue.broker.kafka.network.KafkaHeader;
import org.joyqueue.broker.kafka.network.KafkaPayloadCodec;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.command.Type;
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