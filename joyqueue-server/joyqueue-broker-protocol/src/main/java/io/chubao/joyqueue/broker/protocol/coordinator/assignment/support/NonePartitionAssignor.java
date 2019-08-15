package io.chubao.joyqueue.broker.protocol.coordinator.assignment.support;

import io.chubao.joyqueue.broker.protocol.coordinator.assignment.PartitionAssignor;
import io.chubao.joyqueue.broker.protocol.coordinator.assignment.converter.PartitionAssignmentConverter;
import io.chubao.joyqueue.broker.protocol.coordinator.domain.GroupMemberMetadata;
import io.chubao.joyqueue.broker.protocol.coordinator.domain.GroupMetadata;
import io.chubao.joyqueue.broker.protocol.coordinator.domain.PartitionAssignment;
import io.chubao.joyqueue.domain.PartitionGroup;

import java.util.List;

/**
 * NonePartitionAssignor
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/5
 */
public class NonePartitionAssignor implements PartitionAssignor {

    @Override
    public PartitionAssignment assign(GroupMetadata group, GroupMemberMetadata member, String topic, List<PartitionGroup> partitionGroups) {
        return PartitionAssignmentConverter.convert(partitionGroups);
    }

    @Override
    public String type() {
        return "NONE";
    }
}