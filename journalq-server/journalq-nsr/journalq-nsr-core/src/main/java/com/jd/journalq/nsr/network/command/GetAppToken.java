package com.jd.journalq.nsr.network.command;

import com.jd.journalq.common.network.transport.command.JMQPayload;

/**
 * @author wylixiaobin
 * Date: 2019/2/13
 */
public class GetAppToken extends JMQPayload {
    private String app;
    private String token;
    public GetAppToken app(String app){
        this.app = app;
        return this;
    }
    public GetAppToken token(String token){
        this.token = token;
        return this;
    }

    public String getApp() {
        return app;
    }

    public String getToken() {
        return token;
    }

    @Override
    public int type() {
        return NsrCommandType.GET_APP_TOKEN;
    }

    @Override
    public String toString() {
        return "GetAppToken{" +
                "app='" + app + '\'' +
                ", token='" + token + '\'' +
                '}';
    }
}
