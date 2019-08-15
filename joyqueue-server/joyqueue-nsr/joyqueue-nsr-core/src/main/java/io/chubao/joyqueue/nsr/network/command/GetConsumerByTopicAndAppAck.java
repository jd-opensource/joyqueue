package io.chubao.joyqueue.nsr.network.command;

import io.chubao.joyqueue.domain.Consumer;
import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class GetConsumerByTopicAndAppAck extends JoyQueuePayload {
    private Consumer consumer;
    public GetConsumerByTopicAndAppAck consumer(Consumer consumer){
        this.consumer = consumer;
        return this;
    }

    public Consumer getConsumer() {
        return consumer;
    }

    @Override
    public int type() {
        return NsrCommandType.GET_CONSUMER_BY_TOPIC_AND_APP_ACK;
    }
}
