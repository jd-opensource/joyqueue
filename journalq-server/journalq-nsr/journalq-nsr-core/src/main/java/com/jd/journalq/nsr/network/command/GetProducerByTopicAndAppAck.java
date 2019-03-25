package com.jd.journalq.nsr.network.command;

import com.jd.journalq.domain.Producer;
import com.jd.journalq.network.transport.command.JMQPayload;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class GetProducerByTopicAndAppAck extends JMQPayload {
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
