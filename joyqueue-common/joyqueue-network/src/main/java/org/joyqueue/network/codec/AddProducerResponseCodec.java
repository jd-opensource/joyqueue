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

import com.google.common.collect.Maps;
import org.joyqueue.network.command.AddProducerResponse;
import org.joyqueue.network.command.JoyQueueCommandType;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.codec.JoyQueueHeader;
import org.joyqueue.network.transport.codec.PayloadCodec;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.Map;

/**
 * AddProducerResponse
 *
 * author: gaohaoxiang
 * date: 2018/12/10
 */
public class AddProducerResponseCodec implements PayloadCodec<JoyQueueHeader, AddProducerResponse>, Type {

    @Override
    public AddProducerResponse decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        Map<String, String> result = Maps.newHashMap();
        short producerSize = buffer.readShort();
        for (int i = 0; i < producerSize; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            String producerId = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            result.put(topic, producerId);
        }

        AddProducerResponse addProducerResponse = new AddProducerResponse();
        addProducerResponse.setProducerIds(result);
        return addProducerResponse;
    }

    @Override
    public void encode(AddProducerResponse payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getProducerIds().size());
        for (Map.Entry<String, String> entry : payload.getProducerIds().entrySet()) {
            Serializer.write(entry.getKey(), buffer, Serializer.SHORT_SIZE);
            Serializer.write(entry.getValue(), buffer, Serializer.SHORT_SIZE);
        }
    }

    @Override
    public int type() {
        return JoyQueueCommandType.ADD_PRODUCER_RESPONSE.getCode();
    }
}