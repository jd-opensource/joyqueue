package com.jd.journalq.broker.jmq.coordinator.domain;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jd.journalq.broker.coordinator.domain.CoordinatorGroupMember;
import com.jd.journalq.broker.jmq.coordinator.CoordinatorMemberTimeoutCallback;

import java.util.List;
import java.util.Map;

/**
 * JMQCoordinatorGroupMember
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/5
 */
public class JMQCoordinatorGroupMember extends CoordinatorGroupMember {

    private CoordinatorMemberTimeoutCallback timeoutCallback;
    private Map<String, List<Integer>> assignedPartitionGroups;

    public JMQCoordinatorGroupMember() {
    }

    public JMQCoordinatorGroupMember(String id, String groupId, String connectionId, String connectionHost, int sessionTimeout) {
        super(id, groupId, connectionId, connectionHost, sessionTimeout);
    }

    public void addAssignedPartitionGroups(String topic, int partitionGroupId) {
        getAssignedTopicPartitionGroups(topic).add(partitionGroupId);
    }

    public void removeAssignedPartitionGroups(String topic, int partitionGroupId) {
        if (assignedPartitionGroups == null) {
            return;
        }
        List<Integer> partitionGroups = assignedPartitionGroups.get(topic);
        if (partitionGroups == null) {
            return;
        }
        partitionGroups.remove((Object) partitionGroupId);
    }

    public List<Integer> getAssignedTopicPartitionGroups(String topic) {
        Map<String, List<Integer>> assignments = getOrCreateAssignedPartitionGroups();
        List<Integer> partitionGroupList = assignments.get(topic);
        if (partitionGroupList == null) {
            partitionGroupList = Lists.newArrayList();
            assignments.put(topic, partitionGroupList);
        }
        return partitionGroupList;
    }

    protected Map<String, List<Integer>> getOrCreateAssignedPartitionGroups() {
        if (assignedPartitionGroups == null) {
            assignedPartitionGroups = Maps.newHashMap();
        }
        return assignedPartitionGroups;
    }

    public void setAssignedPartitionGroups(Map<String, List<Integer>> assignedPartitionGroups) {
        this.assignedPartitionGroups = assignedPartitionGroups;
    }

    public Map<String, List<Integer>> getAssignedTopicPartitionGroups() {
        return assignedPartitionGroups;
    }

    public void setTimeoutCallback(CoordinatorMemberTimeoutCallback timeoutCallback) {
        this.timeoutCallback = timeoutCallback;
    }

    public CoordinatorMemberTimeoutCallback getTimeoutCallback() {
        return timeoutCallback;
    }
}