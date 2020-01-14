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

import org.joyqueue.domain.Producer;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.nsr.network.NsrPayloadCodec;
import org.joyqueue.nsr.network.command.GetProducerByTopicAck;
import org.joyqueue.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wylixiaobin
 * Date: 2019/1/29
 */
public class GetProducerByTopicAckCodec implements NsrPayloadCodec<GetProducerByTopicAck>, Type {
    @Override
    public GetProducerByTopicAck decode(Header header, ByteBuf buffer) throws Exception {
        GetProducerByTopicAck getProducerByTopicAck = new GetProducerByTopicAck();
            int size = buffer.readInt();
            List<Producer> list = new ArrayList<>(size);
            for(int i = 0;i<size;i++){
                list.add(Serializer.readProducer(header.getVersion(), buffer));
            }
            getProducerByTopicAck.producers(list);
        return getProducerByTopicAck;
    }

    @Override
    public void encode(GetProducerByTopicAck payload, ByteBuf buffer) throws Exception {
        List<Producer> producers = payload.getProducers();
        if(null==producers){
            buffer.writeInt(0);
            return;
        }
        buffer.writeInt(producers.size());
        for(Producer producer : producers){
            Serializer.write(payload.getHeader().getVersion(), producer,buffer);
        }
    }

    @Override
    public int type() {
        return NsrCommandType.GET_PRODUCER_BY_TOPIC_ACK;
    }
}
