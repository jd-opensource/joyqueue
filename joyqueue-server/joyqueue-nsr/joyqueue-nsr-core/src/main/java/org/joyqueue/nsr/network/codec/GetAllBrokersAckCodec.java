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

import org.joyqueue.domain.Broker;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.nsr.network.NsrPayloadCodec;
import org.joyqueue.nsr.network.command.GetAllBrokersAck;
import org.joyqueue.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class GetAllBrokersAckCodec implements NsrPayloadCodec<GetAllBrokersAck>, Type {
    @Override
    public GetAllBrokersAck decode(Header header, ByteBuf buffer) throws Exception {
        GetAllBrokersAck allBrokersAck = new GetAllBrokersAck();
            int brokerSize = buffer.readInt();
            List<Broker> list = new ArrayList<>(brokerSize);
            //3. broker array
            for(int i =0;i<brokerSize;i++){
                list.add(Serializer.readBroker(buffer));
            }
            allBrokersAck.brokers(list);
        return allBrokersAck;
    }

    @Override
    public void encode(GetAllBrokersAck payload, ByteBuf buffer) throws Exception {
        List<Broker> brokerList = payload.getBrokers();
        if(null==brokerList||brokerList.size()<1){
            buffer.writeInt(0);
            return;
        }
        //2. int
        buffer.writeInt(brokerList.size());
        //3. broker list
        for(Broker broker : brokerList){
            Serializer.write(broker,buffer);
        }
    }

    @Override
    public int type() {
        return NsrCommandType.GET_ALL_BROKERS_ACK;
    }
}
