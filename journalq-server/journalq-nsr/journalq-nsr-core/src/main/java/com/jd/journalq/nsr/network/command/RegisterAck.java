package com.jd.journalq.nsr.network.command;

import com.jd.journalq.common.domain.Broker;
import com.jd.journalq.common.network.transport.command.JMQPayload;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class RegisterAck extends JMQPayload {
    private Broker broker;

    public RegisterAck broker(Broker broker){
        this.broker = broker;
        return this;
    }

    public Broker getBroker() {
        return broker;
    }

    @Override
    public int type() {
        return NsrCommandType.REGISTER_ACK;
    }
}
