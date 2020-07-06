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
import org.joyqueue.broker.kafka.KafkaErrorCode;
import org.joyqueue.broker.kafka.command.OffsetFetchRequest;
import org.joyqueue.broker.kafka.command.OffsetFetchResponse;
import org.joyqueue.broker.kafka.model.OffsetMetadataAndError;
import org.joyqueue.broker.kafka.network.KafkaHeader;
import org.joyqueue.broker.kafka.network.KafkaPayloadCodec;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.Map;

/**
 * OffsetFetchCodec
 *
 * author: gaohaoxiang
 * date: 2018/11/5
 */
public class OffsetFetchCodec implements KafkaPayloadCodec<OffsetFetchResponse>, Type {

    @Override
    public Object decode(KafkaHeader header, ByteBuf buffer) throws Exception {
        OffsetFetchRequest offsetFetchRequest = new OffsetFetchRequest();
        offsetFetchRequest.setGroupId(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        int topicCount = buffer.readInt();
        Map<String, List<Integer>> topicAndPartitions = Maps.newHashMapWithExpectedSize(topicCount);

        for (int i = 0; i < topicCount; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            int partitionCount = buffer.readInt();
            List<Integer> partitions = Lists.newArrayListWithCapacity(partitionCount);
            topicAndPartitions.put(topic, partitions);

            for (int j = 0; j < partitionCount; j++) {
                int partition = buffer.readInt();
                partitions.add(partition);
            }
        }

        offsetFetchRequest.setTopicAndPartitions(topicAndPartitions);
        return offsetFetchRequest;
    }

    @Override
    public void encode(OffsetFetchResponse payload, ByteBuf buffer) throws Exception {
        short version = payload.getVersion();
        if (version >= 3) {
            // throttle_time_ms
            buffer.writeInt(payload.getThrottleTimeMs());
        }

        Map<String, List<OffsetMetadataAndError>> topicOffsetMetadataAndErrors = payload.getTopicMetadataAndErrors();
        // 4字节主题数
        buffer.writeInt(topicOffsetMetadataAndErrors.size());
        for (Map.Entry<String, List<OffsetMetadataAndError>> entry : topicOffsetMetadataAndErrors.entrySet()) {
            Serializer.write(entry.getKey(), buffer, Serializer.SHORT_SIZE);

            // 4字节partition数
            buffer.writeInt(entry.getValue().size());
            for (OffsetMetadataAndError offsetMetadataAndError : entry.getValue()) {
                buffer.writeInt(offsetMetadataAndError.getPartition());
                buffer.writeLong(offsetMetadataAndError.getOffset());
                Serializer.write(offsetMetadataAndError.getMetadata(), buffer, Serializer.SHORT_SIZE);
                buffer.writeShort(offsetMetadataAndError.getError());
            }
        }

        if (version >= 2) {
            // TODO: error code
            buffer.writeShort(KafkaErrorCode.NONE.getCode());
        }
    }

    @Override
    public int type() {
        return KafkaCommandType.OFFSET_FETCH.getCode();
    }
}