package com.jd.journalq.broker.kafka.coordinator;

import com.jd.journalq.broker.coordinator.session.CoordinatorSessionManager;
import com.jd.journalq.domain.Broker;
import com.jd.journalq.domain.PartitionGroup;
import com.jd.journalq.domain.TopicName;

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

    public Broker findGroup(String groupId) {
        return coordinator.findGroup(groupId);
    }

    public boolean isCurrentGroup(String groupId) {
        return coordinator.isCurrentGroup(groupId);
    }

    public Broker findTransaction(String transactionId) {
        return coordinator.findTransaction(transactionId);
    }

    public boolean isCurrentTransaction(String transactionId) {
        return coordinator.isCurrentTransaction(transactionId);
    }

    public PartitionGroup getTransactionPartitionGroup(String transactionId) {
        return coordinator.getTransactionPartitionGroup(transactionId);
    }

    public TopicName getTransactionTopic() {
        return coordinator.getTransactionTopic();
    }

    public CoordinatorSessionManager getSessionManager() {
        return coordinator.getSessionManager();
    }
}