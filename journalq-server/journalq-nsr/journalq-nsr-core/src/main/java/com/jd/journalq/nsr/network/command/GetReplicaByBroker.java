package com.jd.journalq.nsr.network.command;

import com.jd.journalq.network.transport.command.JMQPayload;

/**
 * @author wylixiaobin
 * Date: 2019/1/29
 */
public class GetReplicaByBroker extends JMQPayload {
    private Integer brokerId;
    public GetReplicaByBroker brokerId(int brokerId){
        this.brokerId = brokerId;
        return this;
    }

    public Integer getBrokerId() {
        return brokerId;
    }

    @Override
    public int type() {
        return NsrCommandType.GET_REPLICA_BY_BROKER;
    }

    @Override
    public String toString() {
        return "GetReplicaByBroker{" +
                "brokerId=" + brokerId +
                '}';
    }
}
