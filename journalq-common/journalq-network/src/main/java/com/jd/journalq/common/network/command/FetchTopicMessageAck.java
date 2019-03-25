package com.jd.journalq.common.network.command;

import com.jd.journalq.common.network.transport.command.JMQPayload;

import java.util.Map;

/**
 * FetchTopicMessageAck
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/7
 */
public class FetchTopicMessageAck extends JMQPayload {

    private Map<String, FetchTopicMessageAckData> data;

    @Override
    public int type() {
        return JMQCommandType.FETCH_TOPIC_MESSAGE_ACK.getCode();
    }

    public void setData(Map<String, FetchTopicMessageAckData> data) {
        this.data = data;
    }

    public Map<String, FetchTopicMessageAckData> getData() {
        return data;
    }
}