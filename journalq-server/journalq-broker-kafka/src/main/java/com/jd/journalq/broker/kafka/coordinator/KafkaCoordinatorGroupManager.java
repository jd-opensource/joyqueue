package com.jd.journalq.broker.kafka.coordinator;

import com.jd.journalq.broker.kafka.coordinator.domain.KafkaCoordinatorGroup;
import com.jd.journalq.broker.coordinator.CoordinatorGroupManager;
import com.jd.journalq.broker.kafka.config.KafkaConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GroupMetadataManager
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/5
 */
public class KafkaCoordinatorGroupManager {

    protected static final Logger logger = LoggerFactory.getLogger(KafkaCoordinatorGroupManager.class);

    private KafkaConfig config;
    private CoordinatorGroupManager coordinatorGroupManager;

    public KafkaCoordinatorGroupManager(KafkaConfig config, CoordinatorGroupManager coordinatorGroupManager) {
        this.config = config;
        this.coordinatorGroupManager = coordinatorGroupManager;
    }

    public KafkaCoordinatorGroup getGroup(String groupId) {
        return coordinatorGroupManager.getGroup(groupId);
    }

    public KafkaCoordinatorGroup getOrCreateGroup(KafkaCoordinatorGroup group) {
        return coordinatorGroupManager.getOrCreateGroup(group);
    }

    public boolean removeGroup(KafkaCoordinatorGroup group) {
        coordinatorGroupManager.removeGroup(group.getId());
        return true;
    }

    public boolean removeGroup(String groupId) {
        coordinatorGroupManager.removeGroup(groupId);
        return true;
    }
}