package io.chubao.joyqueue.nsr.network.command;

import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * @author wylixiaobin
 * Date: 2019/1/29
 */
public class GetConsumerByTopic extends JoyQueuePayload {

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
