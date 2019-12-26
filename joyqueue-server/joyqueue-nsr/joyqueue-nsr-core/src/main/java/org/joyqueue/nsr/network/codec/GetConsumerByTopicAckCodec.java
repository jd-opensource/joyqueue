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
package org.joyqueue.nsr.network.codec;

import org.joyqueue.domain.Consumer;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.nsr.network.NsrPayloadCodec;
import org.joyqueue.nsr.network.command.GetConsumerByTopicAck;
import org.joyqueue.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wylixiaobin
 * Date: 2019/1/29
 */
public class GetConsumerByTopicAckCodec implements NsrPayloadCodec<GetConsumerByTopicAck>, Type {
    @Override
    public GetConsumerByTopicAck decode(Header header, ByteBuf buffer) throws Exception {
        GetConsumerByTopicAck getConsumerByTopicAck = new GetConsumerByTopicAck();
            int size = buffer.readInt();
            List<Consumer> list = new ArrayList<>(size);
            for(int i = 0;i<size;i++){
                list.add(Serializer.readConsumer(header.getVersion(), buffer));
            }
            getConsumerByTopicAck.consumers(list);
        return getConsumerByTopicAck;
    }

    @Override
    public void encode(GetConsumerByTopicAck payload, ByteBuf buffer) throws Exception {
        List<Consumer> consumers = payload.getConsumers();
        if(null==consumers||consumers.size()<1){
            buffer.writeInt(0);
            return;
        }
        buffer.writeInt(consumers.size());
        for(Consumer consumer : consumers){
            Serializer.write(payload.getHeader().getVersion(), consumer,buffer);
        }
    }

    @Override
    public int type() {
        return NsrCommandType.GET_CONSUMER_BY_TOPIC_ACK;
    }
}
