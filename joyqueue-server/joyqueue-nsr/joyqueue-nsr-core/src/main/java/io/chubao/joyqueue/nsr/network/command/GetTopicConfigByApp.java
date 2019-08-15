package io.chubao.joyqueue.nsr.network.command;

import io.chubao.joyqueue.domain.Subscription;
import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * @author wylixiaobin
 * Date: 2019/1/28
 */
public class GetTopicConfigByApp extends JoyQueuePayload {
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
