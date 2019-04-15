package com.jd.journalq.broker.kafka.coordinator.group;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Table;
import com.jd.journalq.broker.kafka.command.SyncGroupAssignment;
import com.jd.journalq.broker.kafka.coordinator.Coordinator;
import com.jd.journalq.broker.kafka.coordinator.group.callback.JoinCallback;
import com.jd.journalq.broker.kafka.coordinator.group.callback.SyncCallback;
import com.jd.journalq.broker.kafka.coordinator.group.domain.GroupMetadata;
import com.jd.journalq.broker.kafka.coordinator.group.domain.GroupDescribe;
import com.jd.journalq.broker.kafka.model.OffsetAndMetadata;
import com.jd.journalq.broker.kafka.model.OffsetMetadataAndError;
import com.jd.journalq.domain.Broker;
import com.jd.journalq.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Coordinator
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/7
 */
public class GroupCoordinator extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(GroupCoordinator.class);

    private Coordinator coordinator;
    private GroupBalanceHandler groupBalanceHandler;
    private GroupOffsetHandler groupOffsetHandler;
    private GroupMetadataManager groupMetadataManager;

    public GroupCoordinator(Coordinator coordinator, GroupBalanceHandler groupBalanceHandler, GroupOffsetHandler groupOffsetHandler, GroupMetadataManager groupMetadataManager) {
        this.coordinator = coordinator;
        this.groupBalanceHandler = groupBalanceHandler;
        this.groupOffsetHandler = groupOffsetHandler;
        this.groupMetadataManager = groupMetadataManager;
    }

    public Broker findCoordinator(String groupId) {
        return coordinator.findGroup(groupId);
    }

    public boolean isCurrentCoordinator(String groupId) {
        return coordinator.isCurrentGroup(groupId);
    }

    public void handleJoinGroup(String groupId, String memberId, String clientId, String clientHost, int rebalanceTimeoutMs, int sessionTimeoutMs, String protocolType,
                                Map<String, byte[]> protocols, JoinCallback callback) {
        groupBalanceHandler.joinGroup(groupId, memberId, clientId, clientHost, rebalanceTimeoutMs, sessionTimeoutMs, protocolType, protocols, callback);
    }

    public void handleSyncGroup(String groupId, int generation, String memberId, Map<String, SyncGroupAssignment> groupAssignment, SyncCallback callback) {
        groupBalanceHandler.syncGroup(groupId, generation, memberId, groupAssignment, callback);
    }

    public short handleLeaveGroup(String groupId, String memberId) {
        return groupBalanceHandler.leaveGroup(groupId, memberId);
    }

    public List<GroupDescribe> handleDescribeGroups(List<String> groupIds) {
        return groupBalanceHandler.describeGroups(groupIds);
    }

    public Table<String, Integer, OffsetMetadataAndError> handleCommitOffsets(String groupId, String memberId, int generationId, Table<String, Integer, OffsetAndMetadata> offsetMetadata) {
        return groupOffsetHandler.commitOffsets(groupId, memberId, generationId, offsetMetadata);
    }

    public Table<String, Integer, OffsetMetadataAndError> handleFetchOffsets(String groupId, HashMultimap<String, Integer> topicAndPartitions) {
        return groupOffsetHandler.fetchOffsets(groupId, topicAndPartitions);
    }

    public short handleHeartbeat(String groupId, String memberId, int generationId) {
        return groupBalanceHandler.heartbeat(groupId, memberId, generationId);
    }

    public GroupMetadata getGroup(String groupId) {
        return groupMetadataManager.getGroup(groupId);
    }

    public boolean removeGroup(GroupMetadata group) {
        return groupMetadataManager.removeGroup(group.getId());
    }

    public boolean removeGroup(String groupId) {
        return groupMetadataManager.removeGroup(groupId);
    }
}
