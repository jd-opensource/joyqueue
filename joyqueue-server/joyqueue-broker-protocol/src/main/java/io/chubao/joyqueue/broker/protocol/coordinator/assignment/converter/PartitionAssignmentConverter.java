package io.chubao.joyqueue.broker.protocol.coordinator.assignment.converter;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.broker.protocol.coordinator.domain.PartitionAssignment;
import io.chubao.joyqueue.domain.PartitionGroup;

import java.util.List;

/**
 * PartitionAssignmentConverter
 *
 * author: gaohaoxiang
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