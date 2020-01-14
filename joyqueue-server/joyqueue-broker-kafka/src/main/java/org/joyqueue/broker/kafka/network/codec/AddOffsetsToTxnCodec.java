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

import org.joyqueue.broker.kafka.KafkaCommandType;
import org.joyqueue.broker.kafka.command.AddOffsetsToTxnRequest;
import org.joyqueue.broker.kafka.command.AddOffsetsToTxnResponse;
import org.joyqueue.broker.kafka.network.KafkaHeader;
import org.joyqueue.broker.kafka.network.KafkaPayloadCodec;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * AddOffsetsToTxnCodec
 *
 * author: gaohaoxiang
 * date: 2019/4/4
 */
public class AddOffsetsToTxnCodec implements KafkaPayloadCodec<AddOffsetsToTxnResponse>, Type {

    @Override
    public AddOffsetsToTxnRequest decode(KafkaHeader header, ByteBuf buffer) throws Exception {
        String transactionId = Serializer.readString(buffer, Serializer.SHORT_SIZE);
        long producerId = buffer.readLong();
        short producerEpoch = buffer.readShort();
        String groupId = Serializer.readString(buffer, Serializer.SHORT_SIZE);

        AddOffsetsToTxnRequest addOffsetsToTxnRequest = new AddOffsetsToTxnRequest();
        addOffsetsToTxnRequest.setTransactionId(transactionId);
        addOffsetsToTxnRequest.setProducerId(producerId);
        addOffsetsToTxnRequest.setProducerEpoch(producerEpoch);
        addOffsetsToTxnRequest.setGroupId(groupId);
        return addOffsetsToTxnRequest;
    }

    @Override
    public void encode(AddOffsetsToTxnResponse payload, ByteBuf buffer) throws Exception {
        buffer.writeInt(payload.getThrottleTimeMs());
        buffer.writeShort(payload.getCode());
    }

    @Override
    public int type() {
        return KafkaCommandType.ADD_OFFSETS_TO_TXN.getCode();
    }
}