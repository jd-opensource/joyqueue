package com.jd.journalq.nsr.network.command;

import com.jd.journalq.common.network.transport.command.JMQPayload;

/**
 * @author wylixiaobin
 * Date: 2019/1/29
 */
public class GetBrokerByRetryType extends JMQPayload {

    private String retryType;
    public GetBrokerByRetryType retryType(String retryType){
        this.retryType = retryType;
        return this;
    }

    public String getRetryType() {
        return retryType;
    }

    @Override
    public int type() {
        return NsrCommandType.GET_BROKER_BY_RETRYTYPE;
    }
}
