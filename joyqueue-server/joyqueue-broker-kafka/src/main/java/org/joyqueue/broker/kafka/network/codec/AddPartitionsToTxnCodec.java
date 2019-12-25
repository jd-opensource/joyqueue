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
import org.joyqueue.broker.kafka.command.AddPartitionsToTxnRequest;
import org.joyqueue.broker.kafka.command.AddPartitionsToTxnResponse;
import org.joyqueue.broker.kafka.model.PartitionMetadataAndError;
import org.joyqueue.broker.kafka.network.KafkaHeader;
import org.joyqueue.broker.kafka.network.KafkaPayloadCodec;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.Map;

/**
 * AddPartitionsToTxnCodec
 *
 * author: gaohaoxiang
 * date: 2019/4/4
 */
public class AddPartitionsToTxnCodec implements KafkaPayloadCodec<AddPartitionsToTxnResponse>, Type {

    @Override
    public AddPartitionsToTxnRequest decode(KafkaHeader header, ByteBuf buffer) throws Exception {
        String transactionId = Serializer.readString(buffer, Serializer.SHORT_SIZE);
        long producerId = buffer.readLong();
        short producerEpoch = buffer.readShort();

        int topicSize = Math.max(buffer.readInt(), 0);
        Map<String, List<Integer>> partitions = Maps.newHashMapWithExpectedSize(topicSize);

        for (int i = 0; i < topicSize; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            int partitionSize = Math.max(buffer.readInt(), 0);
            List<Integer> partitionList = Lists.newArrayListWithCapacity(partitionSize);
            for (int j = 0; j < partitionSize; j++) {
                partitionList.add(buffer.readInt());
            }

            partitions.put(topic, partitionList);
        }

        AddPartitionsToTxnRequest addPartitionsToTxnRequest = new AddPartitionsToTxnRequest();
        addPartitionsToTxnRequest.setTransactionId(transactionId);
        addPartitionsToTxnRequest.setProducerId(producerId);
        addPartitionsToTxnRequest.setProducerEpoch(producerEpoch);
        addPartitionsToTxnRequest.setPartitions(partitions);
        return addPartitionsToTxnRequest;
    }

    @Override
    public void encode(AddPartitionsToTxnResponse payload, ByteBuf buffer) throws Exception {
        Map<String, List<PartitionMetadataAndError>> errors = payload.getErrors();

        buffer.writeInt(payload.getThrottleTimeMs());
        buffer.writeInt(errors.size());

        for (Map.Entry<String, List<PartitionMetadataAndError>> entry : errors.entrySet()) {
            Serializer.write(entry.getKey(), buffer, Serializer.SHORT_SIZE);
            buffer.writeInt(entry.getValue().size());
            for (PartitionMetadataAndError partitionMetadataAndError : entry.getValue()) {
                buffer.writeInt(partitionMetadataAndError.getPartition());
                buffer.writeShort(partitionMetadataAndError.getError());
            }
        }
    }

    @Override
    public int type() {
        return KafkaCommandType.ADD_PARTITIONS_TO_TXN.getCode();
    }
}