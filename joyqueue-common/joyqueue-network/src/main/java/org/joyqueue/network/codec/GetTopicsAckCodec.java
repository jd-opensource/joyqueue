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

import org.joyqueue.network.command.CommandType;
import org.joyqueue.network.command.GetTopicsAck;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.codec.PayloadCodec;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.HashSet;
import java.util.Set;

/**
 * @author wylixiaobin
 * Date: 2018/10/19
 */
public class GetTopicsAckCodec implements PayloadCodec<Header, GetTopicsAck>, Type {
    @Override
    public Object decode(Header header, ByteBuf buffer) throws Exception {
        Set<String> topics = new HashSet();
        int topicSize = buffer.readInt();
        for(int i = 0;i<topicSize;i++){
            topics.add(Serializer.readString(buffer));
        }
        return new GetTopicsAck().topics(topics);
    }

    @Override
    public void encode(GetTopicsAck payload, ByteBuf buffer) throws Exception {
        buffer.writeInt(payload.getTopics().size());
        for(String topic : payload.getTopics()){
            Serializer.write(topic,buffer);
        }
    }
    @Override
    public int type() {
        return CommandType.GET_TOPICS_ACK;
    }
}
