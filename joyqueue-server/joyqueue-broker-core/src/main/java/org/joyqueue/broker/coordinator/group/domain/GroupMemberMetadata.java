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
package org.joyqueue.broker.coordinator.group.domain;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joyqueue.toolkit.time.SystemClock;

import java.util.List;
import java.util.Map;

/**
 * GroupMemberMetadata
 *
 * author: gaohaoxiang
 * date: 2018/12/4
 */
public class GroupMemberMetadata {

    private String id;
    private String groupId;
    private String connectionId;
    private String connectionHost;
    private long latestHeartbeat;
    private int sessionTimeout;
    private Map<String, List<Short>> assignments;
    private List<Short> assignmentList;

    public GroupMemberMetadata() {
    }

    public GroupMemberMetadata(String id, String groupId, String connectionId, String connectionHost, int sessionTimeout) {
        this.id = id;
        this.groupId = groupId;
        this.connectionId = connectionId;
        this.connectionHost = connectionHost;
        this.sessionTimeout = sessionTimeout;
    }

    public boolean isExpired() {
        return (latestHeartbeat + sessionTimeout) < SystemClock.now();
    }

    public void setAssignedTopicPartitions(String topic, List<Short> partitions) {
        Map<String, List<Short>> assignments = getOrCreateAssignments();
        assignments.put(topic, partitions);
    }

    public void addAssignedPartition(String topic, short partition) {
        getAssignedTopicPartitions(topic).add(partition);
    }

    public void removeAssignedPartition(String topic, short partition) {
        if (assignments == null) {
            return;
        }
        List<Short> partitions = assignments.get(topic);
        if (partitions == null) {
            return;
        }
        partitions.remove((Object) partition);
    }

    public List<Short> getAssignedTopicPartitions(String topic) {
        Map<String, List<Short>> assignments = getOrCreateAssignments();
        List<Short> partitionList = assignments.get(topic);
        if (partitionList == null) {
            partitionList = Lists.newArrayList();
            assignments.put(topic, partitionList);
        }
        return partitionList;
    }

    protected Map<String, List<Short>> getOrCreateAssignments() {
        if (assignments == null) {
            assignments = Maps.newHashMap();
        }
        return assignments;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public String getConnectionHost() {
        return connectionHost;
    }

    public void setConnectionHost(String connectionHost) {
        this.connectionHost = connectionHost;
    }

    public long getLatestHeartbeat() {
        return latestHeartbeat;
    }

    public void setLatestHeartbeat(long latestHeartbeat) {
        this.latestHeartbeat = latestHeartbeat;
    }

    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public void setAssignments(Map<String, List<Short>> assignments) {
        this.assignments = assignments;
    }

    public Map<String, List<Short>> getAssignments() {
        return assignments;
    }

    public void setAssignmentList(List<Short> assignmentList) {
        this.assignmentList = assignmentList;
    }

    public List<Short> getAssignmentList() {
        return assignmentList;
    }
}