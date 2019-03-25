package com.jd.journalq.broker.jmq.coordinator;

import com.jd.journalq.broker.coordinator.Coordinator;
import com.jd.journalq.domain.Broker;

/**
 * Coordinator
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/4
 */
public class JMQCoordinator {

    private Coordinator coordinator;

    public JMQCoordinator(Coordinator coordinator) {
        this.coordinator = coordinator;
    }

    public boolean isCurrentCoordinator(String app) {
        return coordinator.isCurrentCoordinator(app);
    }

    public Broker findCoordinator(String app) {
        return coordinator.findCoordinator(app);
    }
}