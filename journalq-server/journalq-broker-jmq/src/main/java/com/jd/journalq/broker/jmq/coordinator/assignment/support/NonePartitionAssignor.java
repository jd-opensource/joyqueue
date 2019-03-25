package com.jd.journalq.broker.jmq.coordinator.assignment.support;

import com.jd.journalq.broker.jmq.coordinator.assignment.PartitionAssignor;
import com.jd.journalq.broker.jmq.coordinator.domain.JMQCoordinatorGroup;
import com.jd.journalq.broker.jmq.coordinator.domain.JMQCoordinatorGroupMember;
import com.jd.journalq.broker.jmq.coordinator.domain.PartitionAssignment;
import com.jd.journalq.domain.PartitionGroup;
import com.jd.journalq.broker.jmq.coordinator.assignment.converter.PartitionAssignmentConverter;

import java.util.List;

/**
 * NonePartitionAssignor
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/5
 */
public class NonePartitionAssignor implements PartitionAssignor {

    @Override
    public PartitionAssignment assign(JMQCoordinatorGroup group, JMQCoordinatorGroupMember member, String topic, List<PartitionGroup> partitionGroups) {
        return PartitionAssignmentConverter.convert(partitionGroups);
    }

    @Override
    public String type() {
        return "NONE";
    }
}