package com.jd.journalq.nsr.network.command;

import com.jd.journalq.common.domain.TopicName;
import com.jd.journalq.common.network.transport.command.JMQPayload;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class GetProducerByTopicAndApp extends JMQPayload {
    private TopicName topic;
    private String app;
    public GetProducerByTopicAndApp app(String app){
        this.app = app;
        return this;
    }
    public GetProducerByTopicAndApp topic(TopicName topic){
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
        return NsrCommandType.GET_PRODUCER_BY_TOPIC_AND_APP;
    }

    @Override
    public String toString() {
        return "GetProducerByTopicAndApp{" +
                "topic=" + topic +
                ", app='" + app + '\'' +
                '}';
    }
}
