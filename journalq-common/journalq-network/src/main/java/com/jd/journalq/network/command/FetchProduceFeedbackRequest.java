package com.jd.journalq.network.command;

import com.jd.journalq.network.transport.command.JMQPayload;

/**
 * FetchProduceFeedbackRequest
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/18
 */
public class FetchProduceFeedbackRequest extends JMQPayload {

    private String app;
    private String topic;
    private TxStatus status;
    private int count;
    private int longPollTimeout;

    @Override
    public int type() {
        return JMQCommandType.FETCH_PRODUCE_FEEDBACK.getCode();
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    public TxStatus getStatus() {
        return status;
    }

    public void setStatus(TxStatus status) {
        this.status = status;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public int getLongPollTimeout() {
        return longPollTimeout;
    }

    public void setLongPollTimeout(int longPollTimeout) {
        this.longPollTimeout = longPollTimeout;
    }
}