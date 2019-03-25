package com.jd.journalq.nsr.network.command;

import com.jd.journalq.domain.Producer;
import com.jd.journalq.network.transport.command.JMQPayload;

import java.util.List;

/**
 * @author wylixiaobin
 * Date: 2019/1/29
 */
public class GetProducerByTopicAck extends JMQPayload {
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
