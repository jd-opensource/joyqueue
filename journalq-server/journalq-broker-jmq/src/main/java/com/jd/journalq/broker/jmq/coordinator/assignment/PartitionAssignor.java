package com.jd.journalq.broker.jmq.coordinator.assignment;

import com.jd.journalq.broker.jmq.coordinator.domain.JMQCoordinatorGroup;
import com.jd.journalq.broker.jmq.coordinator.domain.JMQCoordinatorGroupMember;
import com.jd.journalq.broker.jmq.coordinator.domain.PartitionAssignment;
import com.jd.journalq.common.domain.PartitionGroup;
import com.jd.laf.extension.Type;

import java.util.List;

/**
 * PartitionAssignor
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/5
 */
public interface PartitionAssignor extends Type {

    PartitionAssignment assign(JMQCoordinatorGroup group, JMQCoordinatorGroupMember member, String topic, List<PartitionGroup> partitionGroups);
}