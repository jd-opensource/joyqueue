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
import org.joyqueue.network.command.AddProducerRequest;
import org.joyqueue.network.command.JoyQueueCommandType;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.codec.JoyQueueHeader;
import org.joyqueue.network.transport.codec.PayloadCodec;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.List;

/**
 * AddProducerRequestCodec
 *
 * author: gaohaoxiang
 * date: 2018/12/10
 */
public class AddProducerRequestCodec implements PayloadCodec<JoyQueueHeader, AddProducerRequest>, Type {

    @Override
    public AddProducerRequest decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        AddProducerRequest addProducerRequest = new AddProducerRequest();

        short topicSize = buffer.readShort();
        List<String> topics = Lists.newArrayListWithCapacity(topicSize);
        for (int i = 0; i < topicSize; i++) {
            topics.add(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        }

        addProducerRequest.setTopics(topics);
        addProducerRequest.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        addProducerRequest.setSequence(buffer.readLong());
        return addProducerRequest;
    }

    @Override
    public void encode(AddProducerRequest payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getTopics().size());
        for (String topic : payload.getTopics()) {
            Serializer.write(topic, buffer, Serializer.SHORT_SIZE);
        }
        Serializer.write(payload.getApp(), buffer, Serializer.SHORT_SIZE);
        buffer.writeLong(payload.getSequence());
    }

    @Override
    public int type() {
        return JoyQueueCommandType.ADD_PRODUCER_REQUEST.getCode();
    }
}