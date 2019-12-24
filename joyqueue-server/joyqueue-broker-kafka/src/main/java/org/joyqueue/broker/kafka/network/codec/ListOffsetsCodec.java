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
import org.joyqueue.broker.kafka.command.ListOffsetsRequest;
import org.joyqueue.broker.kafka.command.ListOffsetsResponse;
import org.joyqueue.broker.kafka.network.KafkaHeader;
import org.joyqueue.broker.kafka.network.KafkaPayloadCodec;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.Map;

/**
 * ListOffsetsCodec
 *
 * author: gaohaoxiang
 * date: 2018/11/5
 */
public class ListOffsetsCodec implements KafkaPayloadCodec<ListOffsetsResponse>, Type {

    @Override
    public ListOffsetsRequest decode(KafkaHeader header, ByteBuf buffer) throws Exception {
        ListOffsetsRequest offsetRequest = new ListOffsetsRequest();
        offsetRequest.setReplicaId(buffer.readInt());
        if (header.getVersion() >= 2) {
            offsetRequest.setIsolationLevel(buffer.readByte());
        }
        int topicSize = Math.max(buffer.readInt(), 0);
        Map<String, List<ListOffsetsRequest.PartitionOffsetRequest>> partitionRequestMap = Maps.newHashMapWithExpectedSize(topicSize);
        for (int i = 0; i < topicSize; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            int partitionSize = Math.max(buffer.readInt(), 0);
            List<ListOffsetsRequest.PartitionOffsetRequest> partitionRequests = Lists.newArrayListWithCapacity(partitionSize);
            for (int j = 0; j < partitionSize; j++) {
                int partition = buffer.readInt();
                long time = buffer.readLong();
                int maxNumOffsets = header.getApiVersion() == 0 ? buffer.readInt() : 1;
                ListOffsetsRequest.PartitionOffsetRequest partitionOffsetRequest = new ListOffsetsRequest.PartitionOffsetRequest(partition, time, maxNumOffsets);
                partitionRequests.add(partitionOffsetRequest);
            }
            partitionRequestMap.put(topic, partitionRequests);
        }
        offsetRequest.setPartitionRequests(partitionRequestMap);
        return offsetRequest;
    }

    @Override
    public void encode(ListOffsetsResponse payload, ByteBuf buffer) throws Exception {
        short version = payload.getVersion();
        if (version >= 2) {
            // throttle_time_ms
            buffer.writeInt(payload.getThrottleTimeMs());
        }

        // 响应的map
        Map<String, List<ListOffsetsResponse.PartitionOffsetResponse>> partitionResponse = payload.getPartitionResponses();
        // 主题大小
        buffer.writeInt(partitionResponse.size());

        for (Map.Entry<String, List<ListOffsetsResponse.PartitionOffsetResponse>> entry : partitionResponse.entrySet()) {
            String topic = entry.getKey();
            Serializer.write(topic, buffer, Serializer.SHORT_SIZE);

            // partition数量
            buffer.writeInt(entry.getValue().size());
            for (ListOffsetsResponse.PartitionOffsetResponse partitionOffsetResponse : entry.getValue()) {
                // partition编号
                buffer.writeInt(partitionOffsetResponse.getPartition());
                // 错误码
                buffer.writeShort(partitionOffsetResponse.getErrorCode());
                // 时间戳
                if (version >= 1) {
                    buffer.writeLong(partitionOffsetResponse.getTimestamp());
                }

                // 偏移量
                long offset = partitionOffsetResponse.getOffset();
                if (version == 0) {
                    buffer.writeInt(1);
                    buffer.writeLong(offset);
                } else {
                    buffer.writeLong(offset);
                }
            }
        }
    }

    @Override
    public int type() {
        return KafkaCommandType.LIST_OFFSETS.getCode();
    }
}