package io.chubao.joyqueue.nsr.network.command;

import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class GetConsumerByTopicAndApp extends JoyQueuePayload {
    private TopicName topic;
    private String app;
    public GetConsumerByTopicAndApp app(String app){
        this.app = app;
        return this;
    }
    public GetConsumerByTopicAndApp topic(TopicName topic){
        this.topic = topic;
        return this;
    }

    public TopicName getTopic() {
        return topic;
    }

    public String getApp() {
        return app;
    }

    @Override
    public int type() {
        return NsrCommandType.GET_CONSUMER_BY_TOPIC_AND_APP;
    }

    @Override
    public String toString() {
        return "GetConsumerByTopicAndApp{" +
                "topic=" + topic +
                ", app='" + app + '\'' +
                '}';
    }
}
