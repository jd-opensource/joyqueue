package com.jd.journalq.nsr.network.command;

import com.jd.journalq.network.transport.command.JMQPayload;

/**
 * @author wylixiaobin
 * Date: 2019/1/29
 */
public class GetConfigAck extends JMQPayload {
    private String value;
    public GetConfigAck value(String value){
        this.value = value;
        return this;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int type() {
        return NsrCommandType.GET_CONFIG_ACK;
    }
}
