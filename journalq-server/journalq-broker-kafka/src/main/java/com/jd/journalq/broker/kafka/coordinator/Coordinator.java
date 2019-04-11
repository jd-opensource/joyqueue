package com.jd.journalq.broker.kafka.coordinator;

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

    public Broker findGroupCoordinator(String groupId) {
        return coordinator.findGroupCoordinator(groupId);
    }

    public boolean isCurrentGroupCoordinator(String groupId) {
        return coordinator.isCurrentGroupCoordinator(groupId);
    }

    public Broker findTransactionCoordinator(String transactionId) {
        return coordinator.findTransactionCoordinator(transactionId);
    }

    public boolean isCurrentTransactionCoordinator(String transactionId) {
        return coordinator.isCurrentTransactionCoordinator(transactionId);
    }
}