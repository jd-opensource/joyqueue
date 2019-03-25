package com.jd.journalq.nsr.network.codec;

import com.jd.journalq.domain.Replica;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.command.Header;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.nsr.network.NsrPayloadCodec;
import com.jd.journalq.nsr.network.command.GetReplicaByBrokerAck;
import com.jd.journalq.nsr.network.command.NsrCommandType;
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
