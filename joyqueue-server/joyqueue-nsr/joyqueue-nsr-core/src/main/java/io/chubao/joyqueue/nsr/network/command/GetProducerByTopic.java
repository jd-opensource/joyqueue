package io.chubao.joyqueue.nsr.network.command;

import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * @author wylixiaobin
 * Date: 2019/1/29
 */
public class GetProducerByTopic extends JoyQueuePayload {

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
