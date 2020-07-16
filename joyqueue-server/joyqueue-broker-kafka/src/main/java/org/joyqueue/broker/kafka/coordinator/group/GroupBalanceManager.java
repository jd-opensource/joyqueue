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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.joyqueue.broker.kafka.KafkaErrorCode;
import org.joyqueue.broker.kafka.command.SyncGroupAssignment;
import org.joyqueue.broker.kafka.config.KafkaConfig;
import org.joyqueue.broker.kafka.coordinator.group.callback.JoinCallback;
import org.joyqueue.broker.kafka.coordinator.group.delay.DelayedHeartbeat;
import org.joyqueue.broker.kafka.coordinator.group.delay.DelayedInitialJoin;
import org.joyqueue.broker.kafka.coordinator.group.delay.DelayedJoin;
import org.joyqueue.broker.kafka.coordinator.group.domain.GroupMemberMetadata;
import org.joyqueue.broker.kafka.coordinator.group.domain.GroupMetadata;
import org.joyqueue.broker.kafka.coordinator.group.domain.GroupState;
import org.joyqueue.toolkit.delay.DelayedOperationKey;
import org.joyqueue.toolkit.delay.DelayedOperationManager;
import org.joyqueue.toolkit.service.Service;
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * GroupBalanceManager
 *
 * author: gaohaoxiang
 * date: 2018/11/7
 */
public class GroupBalanceManager extends Service {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    private KafkaConfig config;
    private GroupMetadataManager groupMetadataManager;
    private DelayedOperationManager<DelayedJoin> joinPurgatory;
    private DelayedOperationManager<DelayedHeartbeat> heartbeatPurgatory;

    public GroupBalanceManager(KafkaConfig config, GroupMetadataManager groupMetadataManager) {
        this.config = config;
        this.groupMetadataManager = groupMetadataManager;
        this.joinPurgatory = new DelayedOperationManager<>("kafka-rebalance");
        this.heartbeatPurgatory = new DelayedOperationManager<>("kafka-heartbeat");
    }

    @Override
    protected void doStart() {
        joinPurgatory.start();
        heartbeatPurgatory.start();
    }

    @Override
    protected void doStop() {
        joinPurgatory.shutdown();
        heartbeatPurgatory.shutdown();
    }

    public GroupMemberMetadata addMemberAndRebalance(int rebalanceTimeoutMs, int sessionTimeoutMs, String clientId, String clientHost, Map<String, byte[]> protocols,
                                                     GroupMetadata group, JoinCallback callback) {

        String memberId = generateMemberId(group, clientId, clientHost);
        GroupMemberMetadata member = new GroupMemberMetadata(memberId, group.getId(), clientId, clientHost, rebalanceTimeoutMs, sessionTimeoutMs, protocols);

        logger.info("add member, groupId: {}, state: {}, generationId: {}, leaderId: {}, memberId: {}, memberCount = {}, rebalanceTimeout:{}, sessionTimeout:{}",
                group.getId(), group.getState(), group.getGenerationId(), group.getLeaderId(), memberId, group.getAllMemberIds().size(), rebalanceTimeoutMs, sessionTimeoutMs);

        if (group.stateIs(GroupState.PREPARINGREBALANCE) && group.isNewGroup()) {
            group.setNewMemberAdded(true);
        }

        if (group.stateIs(GroupState.STABLE)) {
            group.addExpiredMember(member);
        }

        member.setAwaitingJoinCallback(callback);
        group.addMember(member);
        maybePrepareRebalance(group);

        return member;
    }

    protected String generateMemberId(GroupMetadata group, String clientId, String clientHost) {
        return group.getId() + "-" + clientId + "-" + clientHost + "-" + SystemClock.now();
    }

    public void updateMemberAndRebalance(GroupMetadata group, GroupMemberMetadata member, Map<String, byte[]> protocols, JoinCallback callback) {
        member.setSupportedProtocols(protocols);
        member.setAwaitingJoinCallback(callback);
        maybePrepareRebalance(group);
    }

    public void maybePrepareRebalance(GroupMetadata group) {
        group.inLock(() -> {
            if (group.canRebalance()) {
                prepareRebalance(group);
            }
        });
    }

    public void prepareRebalance(GroupMetadata group) {
        logger.info("prepare rebalance, groupId:{}, state:{}, generationId:{}, leaderId:{}",
                group.getId(), group.getState(), group.getGenerationId(), group.getLeaderId());

        // if any members are awaiting sync, cancel their request and have them rejoin
        if (group.stateIs(GroupState.AWAITINGSYNC)) {
            resetAndPropagateAssignmentError(group, KafkaErrorCode.REBALANCE_IN_PROGRESS.getCode());
        }

        int rebalanceTimeout = config.getRebalanceTimeout() != 0 ? config.getRebalanceTimeout() : group.getMaxRebalanceTimeout();
        int rebalanceDelay = config.getRebalanceInitialDelay();
        DelayedJoin delayedJoin = group.stateIs(GroupState.EMPTY) ?
                new DelayedInitialJoin(this, groupMetadataManager, group, joinPurgatory, rebalanceDelay, rebalanceDelay,
                        Math.max(rebalanceTimeout - rebalanceDelay, 0)) :
                new DelayedJoin(this, groupMetadataManager, group, rebalanceTimeout);

        group.transitionStateTo(GroupState.PREPARINGREBALANCE);

        DelayedOperationKey groupKey = new DelayedOperationKey(group.getId());
        Set<Object> delayedOperationKeys = Sets.newHashSet(groupKey);
        joinPurgatory.tryCompleteElseWatch(delayedJoin, delayedOperationKeys);
    }

    public void setAndPropagateAssignment(GroupMetadata group, Map<String, SyncGroupAssignment> assignment) {
        Preconditions.checkState(group.stateIs(GroupState.AWAITINGSYNC));
        List<GroupMemberMetadata> allMemberMetadata = group.getAllMembers();
        for (GroupMemberMetadata member : allMemberMetadata) {
            SyncGroupAssignment syncGroupAssignment = assignment.get(member.getId());
            Map<String, List<Short>> topicPartitions = Maps.newHashMap();
            for (Map.Entry<String, List<Integer>> entry : syncGroupAssignment.getTopicPartitions().entrySet()) {
                List<Short> partitions = Lists.newArrayListWithCapacity(entry.getValue().size());
                for (Integer partition : entry.getValue()) {
                    partitions.add((short) partition.intValue());
                }
                topicPartitions.put(entry.getKey(), partitions);
            }
            member.setAssignment(syncGroupAssignment);
            member.setAssignments(topicPartitions);
        }
        propagateAssignment(group, KafkaErrorCode.NONE.getCode());
    }

    public void resetAndPropagateAssignmentError(GroupMetadata group, short errorCode) {
        Preconditions.checkState(group.stateIs(GroupState.AWAITINGSYNC));
        List<GroupMemberMetadata> memberMetadatas = group.getAllMembers();
        for (GroupMemberMetadata member : memberMetadatas) {
            member.setAssignment(null);
        }
        propagateAssignment(group, errorCode);
    }

    public void propagateAssignment(GroupMetadata group, short errorCode) {
        List<GroupMemberMetadata> memberMetadatas = group.getAllMembers();
        for (GroupMemberMetadata member : memberMetadatas) {
            if (member.getAwaitingSyncCallback() == null) {
                continue;
            }
            member.getAwaitingSyncCallback().sendResponseCallback(member.getAssignment(), errorCode);
            member.setAwaitingSyncCallback(null);

            // reset the session timeout for members after propagating the member's assignment.
            // This is because if any member's session expired while we were still awaiting either
            // the leader sync group or the storage callback, its expiration will be ignored and no
            // future heartbeat expectations will not be scheduled.
            completeAndScheduleNextHeartbeatExpiration(group, member);
        }
    }

    public void removeMemberAndUpdateGroup(GroupMetadata group, GroupMemberMetadata member) {
        logger.info("member {} in group {} has failed, group state is {}, member count is {}",
                member.getId(), group.getId(), group.getState(), group.getAllMemberIds().size());

        group.removeMember(member.getId());
        switch (group.getState()) {
            case DEAD:
            case EMPTY:
                break;
            case STABLE:
            case AWAITINGSYNC:
                maybePrepareRebalance(group);
                break;
            case PREPARINGREBALANCE:
                DelayedOperationKey groupKey = new DelayedOperationKey(group.getId());
                joinPurgatory.checkAndComplete(groupKey);
                break;
        }
    }

    public void checkAndComplete(GroupMetadata groupMetadata) {
        joinPurgatory.checkAndComplete(new DelayedOperationKey(groupMetadata.getId()));
    }

    public void completeAndScheduleNextHeartbeatExpiration(GroupMetadata group, GroupMemberMetadata member) {
        // complete current heartbeat expectation
        member.setLatestHeartbeat(SystemClock.now());
        DelayedOperationKey memberKey = new DelayedOperationKey(member.getGroupId(), member.getId());
        heartbeatPurgatory.checkAndComplete(memberKey);

        if (logger.isDebugEnabled()) {
            logger.debug("handle heartbeat, group {}, member {}, latestHeartbeat is {}, sessionTimeout is {}",
                    group.getId(), member.getId(), member.getLatestHeartbeat(), member.getSessionTimeout());
        }

        // reschedule the next heartbeat expiration deadline
        long newHeartbeatDeadline = member.getLatestHeartbeat() + member.getSessionTimeout();
        DelayedHeartbeat delayedHeartbeat = new DelayedHeartbeat(this, group, member, newHeartbeatDeadline, member.getSessionTimeout());
        heartbeatPurgatory.tryCompleteElseWatch(delayedHeartbeat, Sets.newHashSet(memberKey));
    }

    public void removeHeartbeatForLeavingMember(GroupMetadata group, GroupMemberMetadata member) {
        member.setLeaving(true);
        DelayedOperationKey memberKey = new DelayedOperationKey(member.getGroupId(), member.getId());
        heartbeatPurgatory.checkAndComplete(memberKey);
    }

    public boolean shouldKeepMemberAlive(GroupMemberMetadata member, long heartbeatDeadline) {
        return member.getAwaitingJoinCallback() != null ||
                member.getAwaitingSyncCallback() != null ||
                member.getLatestHeartbeat() + member.getSessionTimeout() > heartbeatDeadline;
    }
}