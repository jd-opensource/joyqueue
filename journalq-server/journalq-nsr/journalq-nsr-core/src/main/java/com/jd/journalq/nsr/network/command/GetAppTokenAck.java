package com.jd.journalq.nsr.network.command;

import com.jd.journalq.common.domain.AppToken;
import com.jd.journalq.common.network.transport.command.JMQPayload;

/**
 * @author wylixiaobin
 * Date: 2019/2/13
 */
public class GetAppTokenAck extends JMQPayload {
    private AppToken appToken;

    public GetAppTokenAck appToken(AppToken appToken){
        this.appToken = appToken;
        return this;
    }

    public AppToken getAppToken() {
        return appToken;
    }

    @Override
    public int type() {
        return NsrCommandType.GET_APP_TOKEN_ACK;
    }

    @Override
    public String toString() {
        return "GetAppTokenAck{" +
                "appToken=" + appToken +
                '}';
    }
}
