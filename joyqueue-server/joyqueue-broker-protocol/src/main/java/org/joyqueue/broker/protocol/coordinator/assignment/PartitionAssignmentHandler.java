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
package org.joyqueue.broker.protocol.coordinator.assignment;

import com.google.common.collect.Sets;
import org.joyqueue.broker.protocol.config.JoyQueueConfig;
import org.joyqueue.broker.protocol.coordinator.GroupMetadataManager;
import org.joyqueue.broker.protocol.coordinator.assignment.delay.MemberTimeoutDelayedOperation;
import org.joyqueue.broker.protocol.coordinator.domain.GroupMemberMetadata;
import org.joyqueue.broker.protocol.coordinator.domain.GroupMetadata;
import org.joyqueue.broker.protocol.coordinator.domain.PartitionAssignment;
import org.joyqueue.broker.protocol.exception.JoyQueueException;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.toolkit.delay.DelayedOperationKey;
import org.joyqueue.toolkit.delay.DelayedOperationManager;
import org.joyqueue.toolkit.service.Service;
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * PartitionAssignmentHandler
 *
 * author: gaohaoxiang
 * date: 2018/12/5
 */
public class PartitionAssignmentHandler extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(PartitionAssignmentHandler.class);

    private JoyQueueConfig config;
    private GroupMetadataManager coordinatorGroupManager;

    private PartitionAssignorResolver partitionAssignorResolver;
    private DelayedOperationManager memberTimeoutDelayedOperationManager;

    public PartitionAssignmentHandler(JoyQueueConfig config, GroupMetadataManager coordinatorGroupManager) {
        this.config = config;
        this.coordinatorGroupManager = coordinatorGroupManager;
    }

    public PartitionAssignment assign(String topic, String app, String connectionId, String connectionHost, int sessionTimeout, List<PartitionGroup> partitionGroups) {
        GroupMetadata group = coordinatorGroupManager.getGroup(app);
        if (group == null) {
            group = coordinatorGroupManager.getOrCreateGroup(new GroupMetadata(app));
        }
        synchronized (group) {
            return doAssign(group, topic, connectionId, connectionHost, sessionTimeout, partitionGroups);
        }
    }

    protected PartitionAssignment doAssign(GroupMetadata group, String topic, String connectionId, String connectionHost, int sessionTimeout, List<PartitionGroup> partitionGroups) {
        GroupMemberMetadata member = (GroupMemberMetadata) group.getMembers().get(connectionId);
        DelayedOperationKey memberTimeoutDelayedOperationKey = new DelayedOperationKey(connectionId);

        if (member == null) {
            member = new GroupMemberMetadata(connectionId, group.getId(), connectionId, connectionHost, sessionTimeout);
            group.addMember(member);
        } else {
            memberTimeoutDelayedOperationManager.checkAndComplete(memberTimeoutDelayedOperationKey);
        }

        PartitionAssignment assignment = partitionAssignorResolver.assign(group, member, topic, partitionGroups);
        if (assignment == null) {
            throw new JoyQueueException(JoyQueueCode.FW_COORDINATOR_PARTITION_ASSIGNOR_ERROR.getCode());
        }

        member.setLatestHeartbeat(SystemClock.now());
        member.setAssignedTopicPartitions(topic, assignment.getPartitions());

        memberTimeoutDelayedOperationManager.tryCompleteElseWatch(new MemberTimeoutDelayedOperation(group, member,sessionTimeout + config.getCoordinatorPartitionAssignTimeoutOverflow()),
                Sets.newHashSet(memberTimeoutDelayedOperationKey));
        return assignment;
    }

    @Override
    protected void validate() throws Exception {
        partitionAssignorResolver = new PartitionAssignorResolver(config);
        memberTimeoutDelayedOperationManager = new DelayedOperationManager("joyqueue-member-timeout-delayed");
    }

    @Override
    protected void doStart() throws Exception {
        memberTimeoutDelayedOperationManager.start();
    }

    @Override
    protected void doStop() {
        if (memberTimeoutDelayedOperationManager != null) {
            memberTimeoutDelayedOperationManager.shutdown();
        }
    }
}
