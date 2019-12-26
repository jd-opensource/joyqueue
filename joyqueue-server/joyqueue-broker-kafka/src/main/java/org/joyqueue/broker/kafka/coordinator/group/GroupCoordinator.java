/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.broker.kafka.coordinator.group;

import org.joyqueue.broker.kafka.command.SyncGroupAssignment;
import org.joyqueue.broker.kafka.coordinator.Coordinator;
import org.joyqueue.broker.kafka.coordinator.group.callback.JoinCallback;
import org.joyqueue.broker.kafka.coordinator.group.callback.SyncCallback;
import org.joyqueue.broker.kafka.coordinator.group.domain.GroupDescribe;
import org.joyqueue.broker.kafka.coordinator.group.domain.GroupMetadata;
import org.joyqueue.broker.kafka.model.OffsetAndMetadata;
import org.joyqueue.broker.kafka.model.OffsetMetadataAndError;
import org.joyqueue.domain.Broker;
import org.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Coordinator
 *
 * author: gaohaoxiang
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

    public Map<String, List<OffsetMetadataAndError>> handleCommitOffsets(String groupId, String memberId, int generationId, Map<String, List<OffsetAndMetadata>> offsets) {
        return groupOffsetHandler.commitOffsets(groupId, memberId, generationId, offsets);
    }

    public Map<String, List<OffsetMetadataAndError>> handleFetchOffsets(String groupId, Map<String, List<Integer>> topicAndPartitions) {
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
