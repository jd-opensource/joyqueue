package com.jd.journalq.common.network.command;

import com.jd.journalq.common.network.transport.command.JMQPayload;

import java.util.Map;

/**
 * AddConsumerAck
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/10
 */
public class AddConsumerAck extends JMQPayload {

    private Map<String, String> consumerIds;

    @Override
    public int type() {
        return JMQCommandType.ADD_CONSUMER_ACK.getCode();
    }

    public void setConsumerIds(Map<String, String> consumerIds) {
        this.consumerIds = consumerIds;
    }

    public Map<String, String> getConsumerIds() {
        return consumerIds;
    }
}