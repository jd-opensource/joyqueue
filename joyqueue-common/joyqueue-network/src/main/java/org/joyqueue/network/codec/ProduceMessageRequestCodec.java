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
package org.joyqueue.network.codec;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joyqueue.domain.QosLevel;
import org.joyqueue.message.BrokerMessage;
import org.joyqueue.network.command.JoyQueueCommandType;
import org.joyqueue.network.command.ProduceMessageRequest;
import org.joyqueue.network.command.ProduceMessageData;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.codec.JoyQueueHeader;
import org.joyqueue.network.transport.codec.PayloadCodec;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.Map;

/**
 * ProduceMessageRequestCodec
 *
 * author: gaohaoxiang
 * date: 2018/12/18
 */
public class ProduceMessageRequestCodec implements PayloadCodec<JoyQueueHeader, ProduceMessageRequest>, Type {

    @Override
    public ProduceMessageRequest decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        short dataSize = buffer.readShort();
        Map<String, ProduceMessageData> data = Maps.newHashMapWithExpectedSize(dataSize);
        for (int i = 0; i < dataSize; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            String txId = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            int timeout = buffer.readInt();
            QosLevel qosLevel = QosLevel.valueOf(buffer.readByte());

            ProduceMessageData produceMessageData = new ProduceMessageData();
            produceMessageData.setTxId(txId);
            produceMessageData.setTimeout(timeout);
            produceMessageData.setQosLevel(qosLevel);

            short messageSize = buffer.readShort();
            List<BrokerMessage> messages = Lists.newArrayListWithCapacity(messageSize);
            for (int j = 0; j < messageSize; j++) {
                BrokerMessage brokerMessage = Serializer.readBrokerMessage(buffer);
                brokerMessage.setTopic(topic);
                brokerMessage.setTxId(txId);
                messages.add(brokerMessage);
            }

            produceMessageData.setMessages(messages);
            data.put(topic, produceMessageData);
        }

        ProduceMessageRequest produceMessageRequest = new ProduceMessageRequest();
        produceMessageRequest.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        produceMessageRequest.setData(data);
        return produceMessageRequest;
    }

    @Override
    public void encode(ProduceMessageRequest payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getData().size());
        for (Map.Entry<String, ProduceMessageData> entry : payload.getData().entrySet()) {
            ProduceMessageData data = entry.getValue();
            Serializer.write(entry.getKey(), buffer, Serializer.SHORT_SIZE);
            Serializer.write(data.getTxId(), buffer, Serializer.SHORT_SIZE);
            buffer.writeInt(data.getTimeout());
            buffer.writeByte(data.getQosLevel().value());
            buffer.writeShort(data.getMessages().size());
            for (BrokerMessage message : data.getMessages()) {
                Serializer.writeBrokerMessage(message, buffer);
            }
        }
        Serializer.write(payload.getApp(), buffer, Serializer.SHORT_SIZE);
    }

    @Override
    public int type() {
        return JoyQueueCommandType.PRODUCE_MESSAGE_REQUEST.getCode();
    }
}