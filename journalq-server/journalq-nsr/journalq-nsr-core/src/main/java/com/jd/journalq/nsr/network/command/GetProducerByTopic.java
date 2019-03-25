package com.jd.journalq.nsr.network.command;

import com.jd.journalq.common.domain.TopicName;
import com.jd.journalq.common.network.transport.command.JMQPayload;

/**
 * @author wylixiaobin
 * Date: 2019/1/29
 */
public class GetProducerByTopic extends JMQPayload {

    private TopicName topic;
    public GetProducerByTopic topic(TopicName topic){
        this.topic = topic;
        return this;
    }

    public TopicName getTopic() {
        return topic;
    }

    @Override
    public int type() {
        return NsrCommandType.GET_PRODUCER_BY_TOPIC;
    }

    @Override
    public String toString() {
        return "GetProducerByTopic{" +
                "topic=" + topic +
                '}';
    }
}
