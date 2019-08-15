package io.chubao.joyqueue.nsr.network.command;

import io.chubao.joyqueue.domain.Producer;
import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

import java.util.List;

/**
 * @author wylixiaobin
 * Date: 2019/1/29
 */
public class GetProducerByTopicAck extends JoyQueuePayload {
    private List<Producer> producers;

    public GetProducerByTopicAck producers(List<Producer> producers){
        this.producers = producers;
        return this;
    }

    public List<Producer> getProducers() {
        return producers;
    }

    @Override
    public int type() {
        return NsrCommandType.GET_PRODUCER_BY_TOPIC_ACK;
    }
}
