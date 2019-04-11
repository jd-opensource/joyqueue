package com.jd.journalq.network.command;

import com.jd.journalq.network.transport.command.JMQPayload;

import java.util.List;

/**
 * FindCoordinatorRequest
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/3
 */
public class FindCoordinatorRequest extends JMQPayload {

    private List<String> topics;
    private String app;

    @Override
    public int type() {
        return JMQCommandType.FIND_COORDINATOR.getCode();
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