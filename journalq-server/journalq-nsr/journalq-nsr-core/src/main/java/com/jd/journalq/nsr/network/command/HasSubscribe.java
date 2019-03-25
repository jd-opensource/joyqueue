package com.jd.journalq.nsr.network.command;

import com.jd.journalq.common.domain.Subscription;
import com.jd.journalq.common.network.transport.command.JMQPayload;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class HasSubscribe extends JMQPayload {

    private String app;
    private Subscription.Type subscribe;

    public HasSubscribe app(String app){
        this.app = app;
        return this;
    }
    public HasSubscribe subscribe(Subscription.Type subscribe){
        this.subscribe = subscribe;
        return this;
    }

    public String getApp() {
        return app;
    }

    public Subscription.Type getSubscribe() {
        return subscribe;
    }

    @Override
    public int type() {
        return NsrCommandType.HAS_SUBSCRIBE;
    }

    @Override
    public String toString() {
        return "HasSubscribe{" +
                "app='" + app + '\'' +
                ", subscribe=" + subscribe +
                '}';
    }
}
