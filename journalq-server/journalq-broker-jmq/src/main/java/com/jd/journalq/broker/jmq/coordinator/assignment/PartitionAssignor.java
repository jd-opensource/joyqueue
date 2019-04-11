package com.jd.journalq.broker.jmq.coordinator.assignment;

import com.jd.journalq.broker.jmq.coordinator.domain.GroupMetadata;
import com.jd.journalq.broker.jmq.coordinator.domain.GroupMemberMetadata;
import com.jd.journalq.broker.jmq.coordinator.domain.PartitionAssignment;
import com.jd.journalq.domain.PartitionGroup;
import com.jd.laf.extension.Type;

import java.util.List;

/**
 * PartitionAssignor
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/5
 */
public interface PartitionAssignor extends Type {

    PartitionAssignment assign(GroupMetadata group, GroupMemberMetadata member, String topic, List<PartitionGroup> partitionGroups);
}