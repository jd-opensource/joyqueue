package com.jd.journalq.nsr.network.command;

import com.jd.journalq.domain.TopicName;
import com.jd.journalq.network.transport.command.JMQPayload;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class GetTopicConfig extends JMQPayload {
    private TopicName topic;
    public GetTopicConfig topic(TopicName topic){
        this.topic = topic;
        return this;
    }

    public TopicName getTopic() {
        return topic;
    }

    @Override
    public int type() {
        return NsrCommandType.GET_TOPICCONFIG;
    }

    @Override
    public String toString() {
        return "GetTopicConfig{" +
                "topic=" + topic +
                '}';
    }
}
