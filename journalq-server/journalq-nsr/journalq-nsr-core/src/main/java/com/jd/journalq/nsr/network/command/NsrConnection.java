package com.jd.journalq.nsr.network.command;

import com.jd.journalq.common.network.transport.command.JMQPayload;

/**
 * @author wylixiaobin
 * Date: 2019/3/15
 */
public class NsrConnection extends JMQPayload {
    private Integer brokerId;
    public NsrConnection brokerId(Integer brokerId){
        this.brokerId = brokerId;
        return this;
    }

    public Integer getBrokerId() {
        return brokerId;
    }

    @Override
    public int type() {
        return NsrCommandType.CONNECT;
    }
}
