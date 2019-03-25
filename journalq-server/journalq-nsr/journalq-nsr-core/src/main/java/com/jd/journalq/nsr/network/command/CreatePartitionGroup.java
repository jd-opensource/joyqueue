package com.jd.journalq.nsr.network.command;

import com.jd.journalq.common.domain.PartitionGroup;

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
