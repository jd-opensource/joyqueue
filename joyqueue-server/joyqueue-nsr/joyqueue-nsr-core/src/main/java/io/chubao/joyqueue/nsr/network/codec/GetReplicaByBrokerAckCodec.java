package io.chubao.joyqueue.nsr.network.codec;

import io.chubao.joyqueue.domain.Replica;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.command.Header;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.nsr.network.NsrPayloadCodec;
import io.chubao.joyqueue.nsr.network.command.GetReplicaByBrokerAck;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;
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
