package com.jd.journalq.broker.jmq.coordinator;

import com.jd.journalq.broker.jmq.config.JMQConfig;
import com.jd.journalq.broker.jmq.coordinator.domain.JMQCoordinatorGroup;
import com.jd.journalq.broker.coordinator.CoordinatorGroupManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JMQCoordinatorGroupManager
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/5
 */
public class JMQCoordinatorGroupManager {

    protected static final Logger logger = LoggerFactory.getLogger(JMQCoordinatorGroupManager.class);

    private JMQConfig config;
    private CoordinatorGroupManager coordinatorGroupManager;

    public JMQCoordinatorGroupManager(JMQConfig config, CoordinatorGroupManager coordinatorGroupManager) {
        this.config = config;
        this.coordinatorGroupManager = coordinatorGroupManager;
    }

    public JMQCoordinatorGroup getGroup(String groupId) {
        return coordinatorGroupManager.getGroup(groupId);
    }

    public JMQCoordinatorGroup getOrCreateGroup(JMQCoordinatorGroup group) {
        return coordinatorGroupManager.getOrCreateGroup(group);
    }

    public boolean removeGroup(JMQCoordinatorGroup group) {
        coordinatorGroupManager.removeGroup(group.getId());
        return true;
    }

    public boolean removeGroup(String groupId) {
        coordinatorGroupManager.removeGroup(groupId);
        return true;
    }
}