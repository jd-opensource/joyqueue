package com.jd.journalq.broker.kafka.coordinator.group;

import com.jd.journalq.broker.kafka.config.KafkaConfig;
import com.jd.journalq.broker.kafka.coordinator.group.domain.GroupMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GroupMetadataManager
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/5
 */
public class GroupMetadataManager {

    protected static final Logger logger = LoggerFactory.getLogger(GroupMetadataManager.class);

    private KafkaConfig config;
    private com.jd.journalq.broker.coordinator.group.GroupMetadataManager groupMetadataManager;

    public GroupMetadataManager(KafkaConfig config, com.jd.journalq.broker.coordinator.group.GroupMetadataManager groupMetadataManager) {
        this.config = config;
        this.groupMetadataManager = groupMetadataManager;
    }

    public GroupMetadata getGroup(String groupId) {
        return groupMetadataManager.getGroup(groupId);
    }

    public GroupMetadata getOrCreateGroup(GroupMetadata group) {
        return groupMetadataManager.getOrCreateGroup(group);
    }

    public boolean removeGroup(GroupMetadata group) {
        return groupMetadataManager.removeGroup(group.getId());
    }

    public boolean removeGroup(String groupId) {
        return groupMetadataManager.removeGroup(groupId);
    }
}