package io.chubao.joyqueue.nsr.network.command;

import io.chubao.joyqueue.domain.Replica;
import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

import java.util.List;

/**
 * @author wylixiaobin
 * Date: 2019/1/29
 */
public class GetReplicaByBrokerAck extends JoyQueuePayload {
    private List<Replica> replicas;
    public GetReplicaByBrokerAck replicas(List<Replica> replicas){
        this.replicas = replicas;
        return this;
    }

    public List<Replica> getReplicas() {
        return replicas;
    }

    @Override
    public int type() {
        return NsrCommandType.GET_REPLICA_BY_BROKER_ACK;
    }
}
