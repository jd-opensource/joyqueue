package com.jd.journalq.broker.kafka.coordinator;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Table;
import com.jd.journalq.broker.kafka.command.SyncGroupAssignment;
import com.jd.journalq.broker.kafka.coordinator.callback.JoinCallback;
import com.jd.journalq.broker.kafka.coordinator.callback.SyncCallback;
import com.jd.journalq.broker.kafka.coordinator.domain.GroupDescribe;
import com.jd.journalq.broker.kafka.model.OffsetAndMetadata;
import com.jd.journalq.broker.kafka.model.OffsetMetadataAndError;
import com.jd.journalq.common.domain.Broker;
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

    private KafkaCoordinator kafkaCoordinator;
    private GroupBalanceHandler groupBalanceHandler;
    private GroupOffsetHandler groupOffsetHandler;

    public GroupCoordinator(KafkaCoordinator kafkaCoordinator, GroupBalanceHandler groupBalanceHandler, GroupOffsetHandler groupOffsetHandler) {
        this.kafkaCoordinator = kafkaCoordinator;
        this.groupBalanceHandler = groupBalanceHandler;
        this.groupOffsetHandler = groupOffsetHandler;
    }

    public Broker findCoordinator(String group) {
        return kafkaCoordinator.findCoordinator(group);
    }

    public boolean isCurrentCoordinator(String group) {
        return kafkaCoordinator.isCurrentCoordinator(group);
    }

    public void handleJoinGroup(String groupId, String memberId, String clientId, String clientHost, int rebalanceTimeoutMs, int sessionTimeoutMs, String protocolType,
                                Map<String, byte[]> protocols, JoinCallback callback) {
        groupBalanceHandler.handleJoinGroup(groupId, memberId, clientId, clientHost, rebalanceTimeoutMs, sessionTimeoutMs, protocolType, protocols, callback);
    }

    public void handleSyncGroup(String groupId, int generation, String memberId, Map<String, SyncGroupAssignment> groupAssignment, SyncCallback callback) {
        groupBalanceHandler.handleSyncGroup(groupId, generation, memberId, groupAssignment, callback);
    }

    public short handleLeaveGroup(String groupId, String memberId) {
        return groupBalanceHandler.handleLeaveGroup(groupId, memberId);
    }

    public List<GroupDescribe> handleDescribeGroups(List<String> groupIds) {
        return groupBalanceHandler.handleDescribeGroups(groupIds);
    }

    public Table<String, Integer, OffsetMetadataAndError> handleCommitOffsets(String groupId, String memberId, int generationId, Table<String, Integer, OffsetAndMetadata> offsetMetadata) {
        return groupOffsetHandler.handleCommitOffsets(groupId, memberId, generationId, offsetMetadata);
    }

    public Table<String, Integer, OffsetMetadataAndError> handleFetchOffsets(String groupId, HashMultimap<String, Integer> topicAndPartitions) {
        return groupOffsetHandler.handleFetchOffsets(groupId, topicAndPartitions);
    }

    public short handleHeartbeat(String groupId, String memberId, int generationId) {
        return groupBalanceHandler.handleHeartbeat(groupId, memberId, generationId);
    }
}
