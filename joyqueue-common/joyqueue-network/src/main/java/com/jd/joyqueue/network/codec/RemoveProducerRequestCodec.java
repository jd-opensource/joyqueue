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
package com.jd.joyqueue.network.codec;

import com.google.common.collect.Lists;
import com.jd.joyqueue.network.command.JournalqCommandType;
import com.jd.joyqueue.network.command.RemoveProducerRequest;
import com.jd.joyqueue.network.serializer.Serializer;
import com.jd.joyqueue.network.transport.codec.JournalqHeader;
import com.jd.joyqueue.network.transport.codec.PayloadCodec;
import com.jd.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.List;

/**
 * RemoveProducerRequestCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/10
 */
public class RemoveProducerRequestCodec implements PayloadCodec<JournalqHeader, RemoveProducerRequest>, Type {

    @Override
    public RemoveProducerRequest decode(JournalqHeader header, ByteBuf buffer) throws Exception {
        RemoveProducerRequest removeProducerRequest = new RemoveProducerRequest();

        short topicSize = buffer.readShort();
        List<String> topics = Lists.newArrayListWithCapacity(topicSize);
        for (int i = 0; i < topicSize; i++) {
            topics.add(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        }

        removeProducerRequest.setTopics(topics);
        removeProducerRequest.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        return removeProducerRequest;
    }

    @Override
    public void encode(RemoveProducerRequest payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getTopics().size());
        for (String topic : payload.getTopics()) {
            Serializer.write(topic, buffer, Serializer.SHORT_SIZE);
        }
        Serializer.write(payload.getApp(), buffer, Serializer.SHORT_SIZE);
    }

    @Override
    public int type() {
        return JournalqCommandType.REMOVE_PRODUCER_REQUEST.getCode();
    }
}