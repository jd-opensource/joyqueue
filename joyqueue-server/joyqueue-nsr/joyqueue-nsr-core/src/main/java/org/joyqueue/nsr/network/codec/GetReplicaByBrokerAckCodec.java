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

import org.joyqueue.domain.Replica;
import org.joyqueue.domain.TopicName;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.nsr.network.NsrPayloadCodec;
import org.joyqueue.nsr.network.command.GetReplicaByBrokerAck;
import org.joyqueue.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wylixiaobin
 * Date: 2019/1/29
 */
public class GetReplicaByBrokerAckCodec implements NsrPayloadCodec<GetReplicaByBrokerAck>, Type {
    @Override
    public GetReplicaByBrokerAck decode(Header header, ByteBuf buffer) throws Exception {
        GetReplicaByBrokerAck getReplicaByBrokerAck = new GetReplicaByBrokerAck();
            int size = buffer.readInt();
            List<Replica> list = new ArrayList<>(size);
            for(int i = 0 ;i<size;i++){
                Replica replica = new Replica();
                replica.setGroup(buffer.readInt());
                replica.setBrokerId(buffer.readInt());
                replica.setId(Serializer.readString(buffer));
                replica.setTopic(TopicName.parse(Serializer.readString(buffer)));
                list.add(replica);
            }
            getReplicaByBrokerAck.replicas(list);
        return getReplicaByBrokerAck;
    }

    @Override
    public void encode(GetReplicaByBrokerAck payload, ByteBuf buffer) throws Exception {
        List<Replica> replicas = payload.getReplicas();
        if(null==replicas){
            buffer.writeInt(0);
            return;
        }
        buffer.writeInt(replicas.size());
        for(Replica replica : replicas){
            buffer.writeInt(replica.getGroup());
            buffer.writeInt(replica.getBrokerId());
            Serializer.write(replica.getId(),buffer);
            Serializer.write(replica.getTopic().getFullName(),buffer);
        }
    }

    @Override
    public int type() {
        return NsrCommandType.GET_REPLICA_BY_BROKER_ACK;
    }
}
