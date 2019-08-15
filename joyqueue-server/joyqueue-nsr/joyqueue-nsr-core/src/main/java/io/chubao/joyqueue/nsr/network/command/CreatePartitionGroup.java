package io.chubao.joyqueue.nsr.network.command;

import io.chubao.joyqueue.domain.PartitionGroup;

public class CreatePartitionGroup extends OperatePartitionGroup {

    public CreatePartitionGroup(PartitionGroup partitionGroup, boolean rollback) {
        super(partitionGroup, rollback);

    }

    public CreatePartitionGroup(PartitionGroup partitionGroup) {
        super(partitionGroup);
    }

    @Override
    public int type() {
        return NsrCommandType.NSR_CREATE_PARTITIONGROUP;
    }
}
