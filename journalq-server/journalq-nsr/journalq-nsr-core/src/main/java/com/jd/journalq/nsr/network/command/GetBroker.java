package com.jd.journalq.nsr.network.command;

import com.jd.journalq.network.transport.command.JMQPayload;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class GetBroker extends JMQPayload {
    private int brokerId;
    public GetBroker brokerId(int brokerId){
        this.brokerId = brokerId;
        return this;
    }

    public int getBrokerId() {
        return brokerId;
    }

    @Override
    public int type() {
        return NsrCommandType.GET_BROKER;
    }

    @Override
    public String toString() {
        return "GetBroker{" +
                "brokerId=" + brokerId +
                '}';
    }
}
