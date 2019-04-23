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
package com.jd.journalq.broker.coordinator.assignment.support;

import com.google.common.collect.Lists;
import com.jd.journalq.broker.JournalqContext;
import com.jd.journalq.broker.config.JournalqConfig;
import com.jd.journalq.broker.coordinator.CoordinatorMemberTimeoutCallback;
import com.jd.journalq.broker.coordinator.assignment.PartitionAssignor;
import com.jd.journalq.broker.coordinator.assignment.converter.PartitionAssignmentConverter;
import com.jd.journalq.broker.coordinator.domain.JMQCoordinatorGroup;
import com.jd.journalq.broker.coordinator.domain.JMQCoordinatorGroupMember;
import com.jd.journalq.broker.coordinator.domain.PartitionAssignment;
import com.jd.journalq.domain.PartitionGroup;
import com.jd.journalq.broker.coordinator.assignment.domain.PartitionGroupAssignmentMetadata;
import com.jd.journalq.broker.coordinator.assignment.domain.TopicPartitionGroupAssignmentMetadata;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * PartitionGroupBalancePartitionAssignor
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/5
 */
public class PartitionGroupBalancePartitionAssignor implements PartitionAssignor {

    protected static final Logger logger = LoggerFactory.getLogger(PartitionGroupBalancePartitionAssignor.class);

    @Override
    public PartitionAssignment assign(JMQCoordinatorGroup group, JMQCoordinatorGroupMember member, String topic, List<PartitionGroup> partitionGroups) {
        // TODO 去掉静态
        JournalqConfig config = JournalqContext.getConfig();
        int minConnections = config.getCoordinatorPartitionAssignMinConnections();
        TopicPartitionGroupAssignmentMetadata topicPartitionGroupAssignmentMetadata = getOrCreateTopicPartitionGroupAssignMetadata(group, topic);

        // 先释放当前所有分配
        releaseAssigned(group, member, topic);

        // 分配结果
        List<PartitionGroup> assignedPartitionGroups = doAssign(group, topicPartitionGroupAssignmentMetadata, partitionGroups, minConnections);

        // 保存分配结果
        saveAssignedPartitionGroups(topicPartitionGroupAssignmentMetadata, member, topic, assignedPartitionGroups);

        // 添加超时处理
        if (member.getTimeoutCallback() == null) {
            member.setTimeoutCallback(new CoordinatorMemberTimeoutCallback() {
                @Override
                public void onCompletion(JMQCoordinatorGroup group, JMQCoordinatorGroupMember member) {
                    releaseAllAssigned(group, member);
                }
            });
        }

        // 组装分配结果
        return buildPartitionAssignment(member, assignedPartitionGroups);
    }

    protected List<PartitionGroup> doAssign(JMQCoordinatorGroup group, TopicPartitionGroupAssignmentMetadata topicPartitionGroupAssignmentMetadata,
                                            List<PartitionGroup> partitionGroups, int minConnections) {
        // 返回相对空闲的partitionGroup
        List<PartitionGroup> idledPartitionGroups = getIdledPartitionGroups(topicPartitionGroupAssignmentMetadata, partitionGroups, minConnections);

        // 如果有空闲的partitionGroup, 优先分配
        if (CollectionUtils.isNotEmpty(idledPartitionGroups)) {
            return idledPartitionGroups;
        }

        // 否则分配负载相对低的partitionGroup
        int quota = partitionGroups.size() / group.getMembers().size();
        if (quota == 0) {
            quota = 1;
        }
        return getLowLoadPartitionGroups(topicPartitionGroupAssignmentMetadata, partitionGroups, quota);
    }

    protected List<PartitionGroup> getIdledPartitionGroups(TopicPartitionGroupAssignmentMetadata topicPartitionGroupAssignmentMetadata, List<PartitionGroup> partitionGroups, int minConnections) {
        List<PartitionGroup> result = null;
        for (PartitionGroup partitionGroup : partitionGroups) {
            PartitionGroupAssignmentMetadata partitionGroupAssignmentMetadata = getOrCreatePartitionGroupAssignMetadata(topicPartitionGroupAssignmentMetadata, partitionGroup.getGroup());
            if (partitionGroupAssignmentMetadata.getAssigned() < minConnections) {
                if (result == null) {
                    result = Lists.newLinkedList();
                }
                result.add(partitionGroup);
            }
        }
        return result;
    }

    // TODO 优化
    protected List<PartitionGroup> getLowLoadPartitionGroups(TopicPartitionGroupAssignmentMetadata topicPartitionGroupAssignmentMetadata, List<PartitionGroup> partitionGroups, int count) {
        partitionGroups = Lists.newArrayList(partitionGroups);
        Collections.sort(partitionGroups, new Comparator<PartitionGroup>() {
            @Override
            public int compare(PartitionGroup o1, PartitionGroup o2) {
                PartitionGroupAssignmentMetadata o1Metadata = getOrCreatePartitionGroupAssignMetadata(topicPartitionGroupAssignmentMetadata, o1.getGroup());
                PartitionGroupAssignmentMetadata o2Metadata = getOrCreatePartitionGroupAssignMetadata(topicPartitionGroupAssignmentMetadata, o2.getGroup());
                return Integer.compare(o1Metadata.getAssigned(), o2Metadata.getAssigned());
            }
        });

        List<PartitionGroup> result = Lists.newLinkedList();
        for (int i = 0; i < count; i++) {
            result.add(partitionGroups.get(i));
        }
        return result;
    }

    //TODO topic 没有用？
    protected void saveAssignedPartitionGroups(TopicPartitionGroupAssignmentMetadata topicPartitionGroupAssignmentMetadata,
                                               JMQCoordinatorGroupMember member,
                                               String topic,
                                               List<PartitionGroup> assignedPartitionGroups) {
        for (PartitionGroup assignedPartitionGroup : assignedPartitionGroups) {
            assignPartitionGroup(member, topicPartitionGroupAssignmentMetadata, assignedPartitionGroup.getGroup());
        }
    }

    protected PartitionAssignment buildPartitionAssignment(JMQCoordinatorGroupMember member, List<PartitionGroup> assignedPartitionGroups) {
        return PartitionAssignmentConverter.convert(assignedPartitionGroups);
    }

    protected void releaseAllAssigned(JMQCoordinatorGroup group, JMQCoordinatorGroupMember member) {
        for (Map.Entry<String, List<Short>> entry : member.getAssignments().entrySet()) {
            releaseAssigned(group, member, entry.getKey());
        }
    }

    protected void releaseAssigned(JMQCoordinatorGroup group, JMQCoordinatorGroupMember member, String topic) {
        List<Integer> assignedPartitionGroups = member.getAssignedTopicPartitionGroups(topic);
        if (CollectionUtils.isEmpty(assignedPartitionGroups)) {
            return;
        }
        TopicPartitionGroupAssignmentMetadata topicPartitionGroupAssignmentMetadata = getOrCreateTopicPartitionGroupAssignMetadata(group, topic);
        assignedPartitionGroups = Lists.newArrayList(assignedPartitionGroups);
        for (Integer assignedPartitionGroup : assignedPartitionGroups) {
            releasePartitionGroupAssign(member, topicPartitionGroupAssignmentMetadata, assignedPartitionGroup);
        }
    }

    protected void assignPartitionGroup(JMQCoordinatorGroupMember member, TopicPartitionGroupAssignmentMetadata topicPartitionGroupAssignmentMetadata, int partitionGroupId) {
        PartitionGroupAssignmentMetadata partitionGroupAssignmentMetadata = getOrCreatePartitionGroupAssignMetadata(topicPartitionGroupAssignmentMetadata, partitionGroupId);
        partitionGroupAssignmentMetadata.incrAssigned();
        member.addAssignedPartitionGroups(topicPartitionGroupAssignmentMetadata.getTopic(), partitionGroupId);
    }

    protected void releasePartitionGroupAssign(JMQCoordinatorGroupMember member, TopicPartitionGroupAssignmentMetadata topicPartitionGroupAssignmentMetadata, int partitionGroupId) {
        PartitionGroupAssignmentMetadata partitionGroupAssignmentMetadata = getOrCreatePartitionGroupAssignMetadata(topicPartitionGroupAssignmentMetadata, partitionGroupId);
        partitionGroupAssignmentMetadata.decrAssigned();
        member.removeAssignedPartitionGroups(topicPartitionGroupAssignmentMetadata.getTopic(), partitionGroupId);
    }

    protected PartitionGroupAssignmentMetadata getOrCreatePartitionGroupAssignMetadata(TopicPartitionGroupAssignmentMetadata topicPartitionGroupAssignmentMetadata, int partitionGroupId) {
        PartitionGroupAssignmentMetadata partitionGroupAssignmentMetadata = topicPartitionGroupAssignmentMetadata.getPartitionGroups().get(partitionGroupId);
        if (partitionGroupAssignmentMetadata == null) {
            partitionGroupAssignmentMetadata = new PartitionGroupAssignmentMetadata();
            partitionGroupAssignmentMetadata.setPartitionGroupId(partitionGroupId);
            topicPartitionGroupAssignmentMetadata.getPartitionGroups().put(partitionGroupId, partitionGroupAssignmentMetadata);
        }
        return partitionGroupAssignmentMetadata;
    }

    protected TopicPartitionGroupAssignmentMetadata getOrCreateTopicPartitionGroupAssignMetadata(JMQCoordinatorGroup group, String topic) {
        TopicPartitionGroupAssignmentMetadata topicPartitionGroupAssignmentMetadata = (TopicPartitionGroupAssignmentMetadata) group.getAssignContext().get(topic);
        if (topicPartitionGroupAssignmentMetadata == null) {
            topicPartitionGroupAssignmentMetadata = new TopicPartitionGroupAssignmentMetadata();
            topicPartitionGroupAssignmentMetadata.setTopic(topic);
            group.getAssignContext().put(topic, topicPartitionGroupAssignmentMetadata);
        }
        return topicPartitionGroupAssignmentMetadata;
    }

    @Override
    public String type() {
        return "PARTITION_GROUP_BALANCE";
    }
}