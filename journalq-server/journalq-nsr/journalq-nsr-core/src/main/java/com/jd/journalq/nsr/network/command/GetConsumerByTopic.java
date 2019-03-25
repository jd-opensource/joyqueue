package com.jd.journalq.nsr.network.command;

import com.jd.journalq.common.domain.TopicName;
import com.jd.journalq.common.network.transport.command.JMQPayload;

/**
 * @author wylixiaobin
 * Date: 2019/1/29
 */
public class GetConsumerByTopic extends JMQPayload {

    private TopicName topic;
    public GetConsumerByTopic topic(TopicName topic){
        this.topic = topic;
        return this;
    }

    public TopicName getTopic() {
        return topic;
    }

    @Override
    public int type() {
        return NsrCommandType.GET_CONSUMER_BY_TOPIC;
    }

    @Override
    public String toString() {
        return "GetConsumerByTopic{" +
                "topic=" + topic +
                '}';
    }
}
