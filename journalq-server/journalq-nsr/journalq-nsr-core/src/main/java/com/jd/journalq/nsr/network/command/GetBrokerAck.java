package com.jd.journalq.nsr.network.command;

import com.jd.journalq.domain.Broker;
import com.jd.journalq.network.transport.command.JMQPayload;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class GetBrokerAck extends JMQPayload {
    private Broker broker;
    public GetBrokerAck broker(Broker broker){
        this.broker = broker;
        return this;
    }

    public Broker getBroker() {
        return broker;
    }

    @Override
    public int type() {
        return NsrCommandType.GET_BROKER_ACK;
    }
}
