package com.jd.journalq.nsr.network.command;

import com.jd.journalq.common.network.transport.command.JMQPayload;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class HasSubscribeAck extends JMQPayload {

    private boolean have;

    public HasSubscribeAck have(boolean have){
        this.have = have;
        return this;
    }

    public boolean isHave() {
        return have;
    }

    @Override
    public int type() {
        return NsrCommandType.HAS_SUBSCRIBE_ACK;
    }
}
