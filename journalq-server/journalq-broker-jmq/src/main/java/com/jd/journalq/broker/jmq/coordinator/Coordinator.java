package com.jd.journalq.broker.jmq.coordinator;

import com.jd.journalq.domain.Broker;

/**
 * Coordinator
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/4
 */
public class Coordinator {

    private com.jd.journalq.broker.coordinator.Coordinator coordinator;

    public Coordinator(com.jd.journalq.broker.coordinator.Coordinator coordinator) {
        this.coordinator = coordinator;
    }

    public Broker findGroup(String app) {
        return coordinator.findGroup(app);
    }

    public boolean isCurrentGroup(String app) {
        return coordinator.isCurrentGroup(app);
    }
}