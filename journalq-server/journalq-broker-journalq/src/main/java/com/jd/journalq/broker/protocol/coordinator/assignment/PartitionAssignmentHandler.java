/**
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
package com.jd.journalq.broker.protocol.coordinator.assignment;

import com.google.common.collect.Sets;
import com.jd.journalq.broker.protocol.config.JournalqConfig;
import com.jd.journalq.broker.protocol.coordinator.GroupMetadataManager;
import com.jd.journalq.broker.protocol.coordinator.assignment.delay.MemberTimeoutDelayedOperation;
import com.jd.journalq.broker.protocol.coordinator.domain.GroupMemberMetadata;
import com.jd.journalq.broker.protocol.coordinator.domain.GroupMetadata;
import com.jd.journalq.broker.protocol.coordinator.domain.PartitionAssignment;
import com.jd.journalq.broker.protocol.exception.JournalqException;
import com.jd.journalq.domain.PartitionGroup;
import com.jd.journalq.exception.JournalqCode;
import com.jd.journalq.toolkit.delay.DelayedOperationKey;
import com.jd.journalq.toolkit.delay.DelayedOperationManager;
import com.jd.journalq.toolkit.service.Service;
import com.jd.journalq.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * PartitionAssignmentHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/5
 */
public class PartitionAssignmentHandler extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(PartitionAssignmentHandler.class);

    private JournalqConfig config;
    private GroupMetadataManager coordinatorGroupManager;

    private PartitionAssignorResolver partitionAssignorResolver;
    private DelayedOperationManager memberTimeoutDelayedOperationManager;

    public PartitionAssignmentHandler(JournalqConfig config, GroupMetadataManager coordinatorGroupManager) {
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
            throw new JournalqException(JournalqCode.FW_COORDINATOR_PARTITION_ASSIGNOR_ERROR.getCode());
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
        memberTimeoutDelayedOperationManager = new DelayedOperationManager("jmqMemberTimeout");
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
