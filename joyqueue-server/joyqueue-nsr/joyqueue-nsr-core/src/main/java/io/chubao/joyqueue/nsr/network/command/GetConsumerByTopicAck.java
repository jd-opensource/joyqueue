package io.chubao.joyqueue.nsr.network.command;

import io.chubao.joyqueue.domain.Consumer;
import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

import java.util.List;

/**
 * @author wylixiaobin
 * Date: 2019/1/29
 */
public class GetConsumerByTopicAck extends JoyQueuePayload {
    private List<Consumer> consumers;

    public GetConsumerByTopicAck consumers(List<Consumer> consumers){
        this.consumers = consumers;
        return this;
    }

    public List<Consumer> getConsumers() {
        return consumers;
    }

    @Override
    public int type() {
        return NsrCommandType.GET_CONSUMER_BY_TOPIC_ACK;
    }
}
