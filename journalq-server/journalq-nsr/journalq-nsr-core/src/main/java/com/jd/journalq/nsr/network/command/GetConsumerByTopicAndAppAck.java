package com.jd.journalq.nsr.network.command;

import com.jd.journalq.domain.Consumer;
import com.jd.journalq.network.transport.command.JMQPayload;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class GetConsumerByTopicAndAppAck extends JMQPayload {
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
