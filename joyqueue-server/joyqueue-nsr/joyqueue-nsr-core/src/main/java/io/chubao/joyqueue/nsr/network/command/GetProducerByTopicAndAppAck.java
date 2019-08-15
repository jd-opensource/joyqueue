package io.chubao.joyqueue.nsr.network.command;

import io.chubao.joyqueue.domain.Producer;
import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class GetProducerByTopicAndAppAck extends JoyQueuePayload {
    private Producer producer;
    public GetProducerByTopicAndAppAck producer(Producer producer){
        this.producer = producer;
        return this;
    }

    public Producer getProducer() {
        return producer;
    }

    @Override
    public int type() {
        return NsrCommandType.GET_PRODUCER_BY_TOPIC_AND_APP_ACK;
    }
}
