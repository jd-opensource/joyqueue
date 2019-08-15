package io.chubao.joyqueue.broker.protocol.coordinator.assignment.domain;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * TopicPartitionGroupAssignmentMetadata
 *
 * author: gaohaoxiang
 * date: 2018/12/5
 */
public class TopicPartitionGroupAssignmentMetadata {

    private String topic;
    private Map<Integer, PartitionGroupAssignmentMetadata> partitionGroups = Maps.newHashMap();

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    public void setPartitionGroups(Map<Integer, PartitionGroupAssignmentMetadata> partitionGroups) {
        this.partitionGroups = partitionGroups;
    }

    public Map<Integer, PartitionGroupAssignmentMetadata> getPartitionGroups() {
        return partitionGroups;
    }
}