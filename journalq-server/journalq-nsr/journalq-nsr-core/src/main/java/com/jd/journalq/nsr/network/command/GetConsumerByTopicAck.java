package com.jd.journalq.nsr.network.command;

import com.jd.journalq.common.domain.Consumer;
import com.jd.journalq.common.network.transport.command.JMQPayload;

import java.util.List;

/**
 * @author wylixiaobin
 * Date: 2019/1/29
 */
public class GetConsumerByTopicAck extends JMQPayload {
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
