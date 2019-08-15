package io.chubao.joyqueue.broker.protocol.coordinator;

import io.chubao.joyqueue.broker.protocol.config.JoyQueueConfig;
import io.chubao.joyqueue.broker.protocol.coordinator.domain.GroupMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GroupMetadataManager
 *
 * author: gaohaoxiang
 * date: 2018/12/5
 */
public class GroupMetadataManager {

    protected static final Logger logger = LoggerFactory.getLogger(GroupMetadataManager.class);

    private JoyQueueConfig config;
    private io.chubao.joyqueue.broker.coordinator.group.GroupMetadataManager groupMetadataManager;

    public GroupMetadataManager(JoyQueueConfig config, io.chubao.joyqueue.broker.coordinator.group.GroupMetadataManager groupMetadataManager) {
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
        groupMetadataManager.removeGroup(group.getId());
        return true;
    }

    public boolean removeGroup(String groupId) {
        groupMetadataManager.removeGroup(groupId);
        return true;
    }
}