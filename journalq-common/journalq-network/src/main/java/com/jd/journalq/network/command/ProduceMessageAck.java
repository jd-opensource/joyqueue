package com.jd.journalq.network.command;

import com.jd.journalq.network.transport.command.JMQPayload;

import java.util.Map;

/**
 * ProduceMessageAck
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/18
 */
public class ProduceMessageAck extends JMQPayload {

    private Map<String, ProduceMessageAckData> data;

    @Override
    public int type() {
        return JMQCommandType.PRODUCE_MESSAGE_ACK.getCode();
    }

    public void setData(Map<String, ProduceMessageAckData> data) {
        this.data = data;
    }

    public Map<String, ProduceMessageAckData> getData() {
        return data;
    }
}