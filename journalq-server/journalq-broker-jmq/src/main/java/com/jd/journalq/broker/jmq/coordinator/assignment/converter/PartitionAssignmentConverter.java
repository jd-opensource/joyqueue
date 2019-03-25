package com.jd.journalq.broker.jmq.coordinator.assignment.converter;

import com.google.common.collect.Lists;
import com.jd.journalq.broker.jmq.coordinator.domain.PartitionAssignment;
import com.jd.journalq.domain.PartitionGroup;

import java.util.List;

/**
 * PartitionAssignmentConverter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/6
 */
public class PartitionAssignmentConverter {

    public static PartitionAssignment convert(List<PartitionGroup> partitionGroupList) {
        List<Short> partitions = Lists.newLinkedList();
        for (PartitionGroup assignedPartitionGroup : partitionGroupList) {
            for (Short partition : assignedPartitionGroup.getPartitions()) {
                partitions.add(partition);
            }
        }
        PartitionAssignment partitionAssignment = new PartitionAssignment();
        partitionAssignment.setPartitions(partitions);
        return partitionAssignment;
    }
}