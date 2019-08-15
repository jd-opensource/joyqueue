package io.chubao.joyqueue.nsr.network.command;

import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * @author wylixiaobin
 * Date: 2019/1/29
 */
public class GetReplicaByBroker extends JoyQueuePayload {
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
