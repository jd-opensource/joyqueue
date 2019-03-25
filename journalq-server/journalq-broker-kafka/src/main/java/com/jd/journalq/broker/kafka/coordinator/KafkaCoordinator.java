package com.jd.journalq.broker.kafka.coordinator;

import com.jd.journalq.broker.coordinator.Coordinator;
import com.jd.journalq.common.domain.Broker;

/**
 * KafkaCoordinator
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/4
 */
public class KafkaCoordinator {

    private Coordinator coordinator;

    public KafkaCoordinator(Coordinator coordinator) {
        this.coordinator = coordinator;
    }

    public Broker findCoordinator(String group) {
        return coordinator.findCoordinator(group);
    }

    public boolean isCurrentCoordinator(String group) {
        return coordinator.isCurrentCoordinator(group);
    }
}