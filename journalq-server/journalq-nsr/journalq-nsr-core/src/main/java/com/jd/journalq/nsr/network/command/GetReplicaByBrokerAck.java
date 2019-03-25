package com.jd.journalq.nsr.network.command;

import com.jd.journalq.domain.Replica;
import com.jd.journalq.network.transport.command.JMQPayload;

import java.util.List;

/**
 * @author wylixiaobin
 * Date: 2019/1/29
 */
public class GetReplicaByBrokerAck extends JMQPayload {
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
