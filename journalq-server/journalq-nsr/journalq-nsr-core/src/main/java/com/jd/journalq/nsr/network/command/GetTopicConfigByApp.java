package com.jd.journalq.nsr.network.command;

import com.jd.journalq.domain.Subscription;
import com.jd.journalq.network.transport.command.JMQPayload;

/**
 * @author wylixiaobin
 * Date: 2019/1/28
 */
public class GetTopicConfigByApp extends JMQPayload {
    private String app;
    private Subscription.Type subscribe;
    public GetTopicConfigByApp app(String app){
        this.app = app;
        return this;
    }
    public GetTopicConfigByApp subscribe(Subscription.Type subscribe){
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
        return NsrCommandType.GET_TOPICCONFIGS_BY_APP;
    }

    @Override
    public String toString() {
        return "GetTopicConfigByApp{" +
                "app='" + app + '\'' +
                ", subscribe=" + subscribe +
                '}';
    }
}
