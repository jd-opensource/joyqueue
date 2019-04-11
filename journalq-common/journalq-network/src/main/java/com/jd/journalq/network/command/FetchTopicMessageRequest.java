package com.jd.journalq.network.command;

import com.jd.journalq.network.transport.command.JMQPayload;

import java.util.Map;

/**
 * FetchTopicMessageRequest
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/7
 */
public class FetchTopicMessageRequest extends JMQPayload {

    private Map<String, FetchTopicMessageData> topics;
    private String app;
    private int ackTimeout;
    private int longPollTimeout;

    @Override
    public int type() {
        return JMQCommandType.FETCH_TOPIC_MESSAGE.getCode();
    }

    public void setTopics(Map<String, FetchTopicMessageData> topics) {
        this.topics = topics;
    }

    public Map<String, FetchTopicMessageData> getTopics() {
        return topics;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getApp() {
        return app;
    }

    public void setAckTimeout(int ackTimeout) {
        this.ackTimeout = ackTimeout;
    }

    public int getAckTimeout() {
        return ackTimeout;
    }

    public int getLongPollTimeout() {
        return longPollTimeout;
    }

    public void setLongPollTimeout(int longPollTimeout) {
        this.longPollTimeout = longPollTimeout;
    }
}
