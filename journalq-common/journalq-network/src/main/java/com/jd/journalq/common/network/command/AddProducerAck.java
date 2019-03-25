package com.jd.journalq.common.network.command;

import com.jd.journalq.common.network.transport.command.JMQPayload;

import java.util.Map;

/**
 * AddProducerAck
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/10
 */
public class AddProducerAck extends JMQPayload {

    private Map<String, String> producerIds;

    @Override
    public int type() {
        return JMQCommandType.ADD_PRODUCER_ACK.getCode();
    }

    public void setProducerIds(Map<String, String> producerIds) {
        this.producerIds = producerIds;
    }

    public Map<String, String> getProducerIds() {
        return producerIds;
    }
}