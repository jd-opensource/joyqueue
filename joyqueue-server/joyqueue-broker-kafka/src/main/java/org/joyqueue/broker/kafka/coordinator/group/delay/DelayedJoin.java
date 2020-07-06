/**
 * Partially copied from Apache Kafka.
 *
 * Original LICENSE :
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.broker.kafka.coordinator.group.delay;

import org.apache.commons.collections.CollectionUtils;
import org.joyqueue.broker.kafka.KafkaErrorCode;
import org.joyqueue.broker.kafka.coordinator.group.GroupBalanceManager;
import org.joyqueue.broker.kafka.coordinator.group.GroupMetadataManager;
import org.joyqueue.broker.kafka.coordinator.group.domain.GroupJoinGroupResult;
import org.joyqueue.broker.kafka.coordinator.group.domain.GroupMemberMetadata;
import org.joyqueue.broker.kafka.coordinator.group.domain.GroupMetadata;
import org.joyqueue.broker.kafka.coordinator.group.domain.GroupState;
import org.joyqueue.toolkit.delay.DelayedOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DelayedJoin extends DelayedOperation {

    protected static final Logger logger = LoggerFactory.getLogger(DelayedJoin.class);

    private GroupBalanceManager groupBalanceManager;
    private GroupMetadataManager groupMetadataManager;
    private GroupMetadata group;

    public DelayedJoin(GroupBalanceManager groupBalanceManager, GroupMetadataManager groupMetadataManager, GroupMetadata group, long rebalanceTimeout) {
        super(rebalanceTimeout, group.getLock());
        this.groupBalanceManager = groupBalanceManager;
        this.groupMetadataManager = groupMetadataManager;
        this.group = group;
    }

    @Override
    protected boolean tryComplete() {
        return group.inLock(() -> {
            if (group.getNotYetRejoinedMembers().isEmpty()) {
                return forceComplete();
            } else {
                return false;
            }
        });
    }

    @Override
    protected void onExpiration() {
        logger.debug("group {} expire join.", group.getId());
    }

    @Override
    protected void onComplete() {
        group.inLock(() -> {
            doComplete();
        });
    }

    protected void doComplete() {
        logger.info("group {} delay join", group.getId());

        List<GroupMemberMetadata> failedMembers = group.getNotYetRejoinedMembers();
        // 移除未join的member
        if (CollectionUtils.isNotEmpty(failedMembers)) {
            for (GroupMemberMetadata memberMetadata : failedMembers) {
                logger.info("group {} complete join, member {} not join.", group.getId(), memberMetadata.getId());
                group.removeMember(memberMetadata.getId());
            }
        }

        if (group.isMemberEmpty()) {
            logger.info("group {} generation {} is dead and removed", group.getId(), group.getGenerationId());
            group.reset();
            return;
        }

        // join成功
        if (!group.stateIs(GroupState.DEAD)) {
            group.initNextGeneration();
            logger.info("stabilized group {} generation {}, member count is {}", group.getId(), group.getGenerationId(), group.getAllMemberIds().size());

            for (GroupMemberMetadata memberMetadata : group.getAllMembers()) {
                Map<String, byte[]> members = null;
                if (memberMetadata.getId().equals(group.getLeaderId())) {
                    members = group.currentMemberMetadata();
                } else {
                    members = Collections.emptyMap();
                }

                GroupJoinGroupResult groupJoinGroupResult = new GroupJoinGroupResult(members, memberMetadata.getId(), group.getGenerationId(), group.getProtocol(),
                        group.getLeaderId(), KafkaErrorCode.NONE.getCode());
                memberMetadata.getAwaitingJoinCallback().sendResponseCallback(groupJoinGroupResult);
                memberMetadata.setAwaitingJoinCallback(null);

                groupBalanceManager.completeAndScheduleNextHeartbeatExpiration(group, memberMetadata);
            }
        }
    }
}

