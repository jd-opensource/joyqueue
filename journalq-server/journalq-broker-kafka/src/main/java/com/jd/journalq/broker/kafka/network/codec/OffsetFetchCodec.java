/**
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
package com.jd.journalq.broker.kafka.network.codec;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Table;
import com.jd.journalq.broker.kafka.network.KafkaHeader;
import com.jd.journalq.broker.kafka.network.KafkaPayloadCodec;
import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.KafkaErrorCode;
import com.jd.journalq.broker.kafka.command.OffsetFetchRequest;
import com.jd.journalq.broker.kafka.command.OffsetFetchResponse;
import com.jd.journalq.broker.kafka.model.OffsetMetadataAndError;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.Map;
import java.util.Set;

/**
 * OffsetFetchCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/5
 */
public class OffsetFetchCodec implements KafkaPayloadCodec<OffsetFetchResponse>, Type {

    @Override
    public Object decode(KafkaHeader header, ByteBuf buffer) throws Exception {
        HashMultimap<String, Integer> topicAndPartitions = HashMultimap.create();
        OffsetFetchRequest offsetFetchRequest = new OffsetFetchRequest();
        offsetFetchRequest.setGroupId(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        int topicCount = buffer.readInt();
        for (int i = 0; i < topicCount; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            int partitionCount = buffer.readInt();
            for (int j = 0; j < partitionCount; j++) {
                int partitionId = buffer.readInt();
                topicAndPartitions.put(topic, partitionId);
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

        Table<String, Integer, OffsetMetadataAndError> topicOffsetMetadataAndErrors = payload.getTopicMetadataAndErrors();
        Set<String> topics = topicOffsetMetadataAndErrors.rowKeySet();
        // 4字节主题数
        buffer.writeInt(topics.size());
        for (String topic : topics) {
            Serializer.write(topic, buffer, Serializer.SHORT_SIZE);

            Map<Integer, OffsetMetadataAndError> partitionOffsetMetadatAndErrors = topicOffsetMetadataAndErrors.row(topic);
            Set<Integer> partitions = partitionOffsetMetadatAndErrors.keySet();
            // 4字节partition数
            buffer.writeInt(partitions.size());
            for (int partition : partitions) {
                buffer.writeInt(partition);
                OffsetMetadataAndError offsetMetadataAndError = partitionOffsetMetadatAndErrors.get(partition);
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