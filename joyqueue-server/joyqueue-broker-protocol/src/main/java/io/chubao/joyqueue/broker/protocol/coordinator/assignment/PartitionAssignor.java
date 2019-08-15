package io.chubao.joyqueue.broker.protocol.coordinator.assignment;

import io.chubao.joyqueue.broker.protocol.coordinator.domain.GroupMemberMetadata;
import io.chubao.joyqueue.broker.protocol.coordinator.domain.GroupMetadata;
import io.chubao.joyqueue.broker.protocol.coordinator.domain.PartitionAssignment;
import io.chubao.joyqueue.domain.PartitionGroup;
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
