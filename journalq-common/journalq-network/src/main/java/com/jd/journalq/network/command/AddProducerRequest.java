package com.jd.journalq.network.command;

import com.jd.journalq.network.transport.command.JMQPayload;

import java.util.List;

/**
 * AddProducerRequest
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/10
 */
public class AddProducerRequest extends JMQPayload {

    private List<String> topics;
    private String app;
    private long sequence;

    @Override
    public int type() {
        return JMQCommandType.ADD_PRODUCER.getCode();
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    public List<String> getTopics() {
        return topics;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getApp() {
        return app;
    }

    public void setSequence(long sequence) {
        this.sequence = sequence;
    }

    public long getSequence() {
        return sequence;
    }
}