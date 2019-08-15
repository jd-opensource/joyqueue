package io.chubao.joyqueue.nsr.network.command;

import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class GetTopicConfig extends JoyQueuePayload {
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
