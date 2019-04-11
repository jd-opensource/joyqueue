package com.jd.journalq.network.command;

import com.jd.journalq.network.transport.command.JMQPayload;

import java.util.List;

/**
 * RemoveProducerRequest
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/10
 */
public class RemoveProducerRequest extends JMQPayload {

    private List<String> topics;
    private String app;

    @Override
    public int type() {
        return JMQCommandType.REMOVE_PRODUCER.getCode();
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
}