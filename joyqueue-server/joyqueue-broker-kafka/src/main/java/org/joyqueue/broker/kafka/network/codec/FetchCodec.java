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
import org.joyqueue.broker.kafka.command.FetchRequest;
import org.joyqueue.broker.kafka.command.FetchResponse;
import org.joyqueue.broker.kafka.message.KafkaMessageSerializer;
import org.joyqueue.broker.kafka.network.KafkaHeader;
import org.joyqueue.broker.kafka.network.KafkaPayloadCodec;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.network.transport.exception.TransportException;
import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.Map;

/**
 * FetchCodec
 *
 * author: gaohaoxiang
 * date: 2018/11/5
 */
public class FetchCodec implements KafkaPayloadCodec<FetchResponse>, Type {

    @Override
    public FetchRequest decode(KafkaHeader header, ByteBuf buffer) throws Exception {
        FetchRequest fetchRequest = new FetchRequest();
        fetchRequest.setReplicaId(buffer.readInt());
        fetchRequest.setMaxWait(buffer.readInt());
        fetchRequest.setMinBytes(buffer.readInt());
        if (header.getApiVersion() >= 3) {
            // max_bytes
            fetchRequest.setMaxBytes(buffer.readInt());
        }
        if (header.getApiVersion() >= 4) {
            // isolation_level
            fetchRequest.setIsolationLevel(buffer.readByte());
        }
        int topicSize = Math.max(buffer.readInt(), 0);
        Map<String, List<FetchRequest.PartitionRequest>> partitionRequestMap = Maps.newHashMapWithExpectedSize(topicSize);

        for (int i = 0; i < topicSize; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            int partitionSize = Math.max(buffer.readInt(), 0);
            List<FetchRequest.PartitionRequest> partitionRequests = partitionRequestMap.get(topic);
            if (partitionRequests == null) {
                partitionRequests = Lists.newArrayListWithCapacity(partitionSize);
                partitionRequestMap.put(topic, partitionRequests);
            }

            for (int j = 0; j < partitionSize; j++) {
                int partitionId = buffer.readInt();
                long offset = buffer.readLong();
                long logStartOffset = 0;
                if (header.getApiVersion() >= 5) {
                    // log_start_offset
                    logStartOffset = buffer.readLong();
                }
                int partitionMaxBytes = buffer.readInt();

                FetchRequest.PartitionRequest partitionRequest = new FetchRequest.PartitionRequest();
                partitionRequest.setPartition(partitionId);
                partitionRequest.setOffset(offset);
                partitionRequest.setMaxBytes(partitionMaxBytes);
                if (header.getApiVersion() >= 5) {
                    partitionRequest.setLogStartOffset(logStartOffset);
                }
                partitionRequests.add(partitionRequest);
            }
        }
        fetchRequest.setPartitionRequests(partitionRequestMap);
        return fetchRequest;
    }

    @Override
    public void encode(FetchResponse payload, ByteBuf buffer) throws Exception {
        short version = payload.getVersion();
        if (version >= 1) {
            buffer.writeInt(payload.getThrottleTimeMs());
        }
        Map<String, List<FetchResponse.PartitionResponse>> partitionResponseMap = payload.getPartitionResponses();
        buffer.writeInt(partitionResponseMap.size());

        for (Map.Entry<String, List<FetchResponse.PartitionResponse>> entry : partitionResponseMap.entrySet()) {
            try {
                Serializer.write(entry.getKey(), buffer, Serializer.SHORT_SIZE);
            } catch (Exception e) {
                throw new TransportException.CodecException(e);
            }
            List<FetchResponse.PartitionResponse> partitionResponses = entry.getValue();
            buffer.writeInt(partitionResponses.size());

            for (FetchResponse.PartitionResponse partitionResponse : partitionResponses) {
                buffer.writeInt(partitionResponse.getPartition());
                buffer.writeShort(partitionResponse.getError());
                buffer.writeLong(partitionResponse.getHighWater());

                // not fully supported, just make it compatible
                if (version >= 4) {
                    // last_stable_offset
                    buffer.writeLong(partitionResponse.getLastStableOffset());

                    // log_start_offset
                    if (version >= 5) {
                        buffer.writeLong(partitionResponse.getLogStartOffset());
                    }

                    // aborted_transactions
                    // size
                    buffer.writeInt(0);
                    // producer_id
                    // first_offset
                }

                int startIndex = buffer.writerIndex();
                buffer.writeInt(0); // length

                KafkaMessageSerializer.writeMessages(buffer, partitionResponse.getMessages(), payload.getVersion());

                int length = buffer.writerIndex() - startIndex - 4;
                buffer.setInt(startIndex, length);
            }
        }
    }

    @Override
    public int type() {
        return KafkaCommandType.FETCH.getCode();
    }
}