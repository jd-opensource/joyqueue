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

import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.nsr.network.NsrPayloadCodec;
import org.joyqueue.nsr.network.command.GetConsumerByTopicAndAppAck;
import org.joyqueue.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class GetConsumerByTopicAndAppAckCodec implements NsrPayloadCodec<GetConsumerByTopicAndAppAck>, Type {
    @Override
    public GetConsumerByTopicAndAppAck decode(Header header, ByteBuf buffer) throws Exception {
        GetConsumerByTopicAndAppAck getConsumerByTopicAndAppAck = new GetConsumerByTopicAndAppAck();
        if(buffer.readBoolean()){
            getConsumerByTopicAndAppAck.consumer(Serializer.readConsumer(header.getVersion(), buffer));
        }
        return getConsumerByTopicAndAppAck;
    }

    @Override
    public void encode(GetConsumerByTopicAndAppAck payload, ByteBuf buffer) throws Exception {
        if(null==payload.getConsumer()){
            buffer.writeBoolean(false);
            return;
        }
        buffer.writeBoolean(true);
        Serializer.write(payload.getHeader().getVersion(), payload.getConsumer(),buffer);
    }

    @Override
    public int type() {
        return NsrCommandType.GET_CONSUMER_BY_TOPIC_AND_APP_ACK;
    }
}
