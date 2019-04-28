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
package com.jd.journalq.network.codec;

import com.google.common.collect.Maps;
import com.jd.journalq.network.command.AddConsumerAck;
import com.jd.journalq.network.command.JournalqCommandType;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.Map;

/**
 * AddConsumerAckCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/10
 */
public class AddConsumerAckCodec implements PayloadCodec<JMQHeader, AddConsumerAck>, Type {

    @Override
    public AddConsumerAck decode(JMQHeader header, ByteBuf buffer) throws Exception {
        Map<String, String> result = Maps.newHashMap();
        short consumerSize = buffer.readShort();
        for (int i = 0; i < consumerSize; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            String consumerId = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            result.put(topic, consumerId);
        }

        AddConsumerAck addConsumerAck = new AddConsumerAck();
        addConsumerAck.setConsumerIds(result);
        return addConsumerAck;
    }

    @Override
    public void encode(AddConsumerAck payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getConsumerIds().size());
        for (Map.Entry<String, String> entry : payload.getConsumerIds().entrySet()) {
            Serializer.write(entry.getKey(), buffer, Serializer.SHORT_SIZE);
            Serializer.write(entry.getValue(), buffer, Serializer.SHORT_SIZE);
        }
    }

    @Override
    public int type() {
        return JournalqCommandType.ADD_CONSUMER_ACK.getCode();
    }
}