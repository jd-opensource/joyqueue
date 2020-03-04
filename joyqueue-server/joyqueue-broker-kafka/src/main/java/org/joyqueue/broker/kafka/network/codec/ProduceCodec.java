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
import io.netty.buffer.ByteBuf;
import org.joyqueue.broker.kafka.KafkaCommandType;
import org.joyqueue.broker.kafka.command.ProduceRequest;
import org.joyqueue.broker.kafka.command.ProduceResponse;
import org.joyqueue.broker.kafka.message.KafkaBrokerMessage;
import org.joyqueue.broker.kafka.message.KafkaMessageSerializer;
import org.joyqueue.broker.kafka.network.KafkaHeader;
import org.joyqueue.broker.kafka.network.KafkaPayloadCodec;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.network.transport.exception.TransportException;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

/**
 * ProduceCodec
 *
 * author: gaohaoxiang
 * date: 2018/11/5
 */
public class ProduceCodec implements KafkaPayloadCodec<ProduceResponse>, Type {

    @Override
    public ProduceRequest decode(KafkaHeader header, ByteBuf buffer) throws Exception {
        ProduceRequest produceRequest = new ProduceRequest();

        if (header.getVersion() >= 3) {
            produceRequest.setTransactionalId(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        }

        produceRequest.setRequiredAcks(buffer.readShort());
        produceRequest.setAckTimeoutMs(buffer.readInt());
        int topicSize = Math.max(buffer.readInt(), 0);
        Map<String, List<ProduceRequest.PartitionRequest>> partitionRequestMap = Maps.newHashMapWithExpectedSize(topicSize);
        int partitionNum = 0;

        for (int i = 0; i < topicSize; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            int partitionSize = Math.max(buffer.readInt(), 0);
            partitionNum += partitionSize;
            List<ProduceRequest.PartitionRequest> partitionRequests = Lists.newArrayListWithCapacity(partitionSize);

            for (int j = 0; j < partitionSize; j++) {
                int partition = buffer.readInt();
                int messageSetSize = buffer.readInt();

                byte[] bytes = new byte[messageSetSize];
                buffer.readBytes(bytes);

                List<KafkaBrokerMessage> messages = KafkaMessageSerializer.readMessages(ByteBuffer.wrap(bytes));
                if (!produceRequest.isTransaction()) {
                    for (KafkaBrokerMessage message : messages) {
                        if (message.isTransaction()) {
                            produceRequest.setTransaction(true);
                            produceRequest.setProducerId(message.getProducerId());
                            produceRequest.setProducerEpoch(message.getProducerEpoch());
                        }
                    }
                }
                partitionRequests.add(new ProduceRequest.PartitionRequest(partition, messages));
            }

            partitionRequestMap.put(topic, partitionRequests);
        }

        produceRequest.setPartitionRequests(partitionRequestMap);
        produceRequest.setPartitionNum(partitionNum);
        return produceRequest;
    }

    @Override
    public void encode(ProduceResponse payload, ByteBuf buffer) throws Exception {
        int version = payload.getVersion();
        buffer.writeInt(payload.getPartitionResponses().size());
        for (Map.Entry<String, List<ProduceResponse.PartitionResponse>> entry : payload.getPartitionResponses().entrySet()) {
            try {
                Serializer.write(entry.getKey(), buffer, Serializer.SHORT_SIZE);
            } catch (Exception e) {
                throw new TransportException.CodecException(e);
            }
            buffer.writeInt(entry.getValue().size());
            for (ProduceResponse.PartitionResponse partitionResponse : entry.getValue()) {
                buffer.writeInt(partitionResponse.getPartition());
                buffer.writeShort(partitionResponse.getErrorCode());
                buffer.writeLong(partitionResponse.getOffset());
                if (version >= 2) {
                    // log_append_time
                    buffer.writeLong(partitionResponse.getLogAppendTime());
                }
                if (version >= 5) {
                    // log_start_offset
                    buffer.writeLong(partitionResponse.getLogStartOffset());
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