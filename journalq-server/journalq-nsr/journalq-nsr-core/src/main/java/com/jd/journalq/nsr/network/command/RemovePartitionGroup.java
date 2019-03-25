package com.jd.journalq.nsr.network.command;

import com.jd.journalq.domain.PartitionGroup;

public class RemovePartitionGroup extends OperatePartitionGroup {
    public RemovePartitionGroup(PartitionGroup partitionGroup, boolean rollback) {
        super(partitionGroup, rollback);
    }

    public RemovePartitionGroup(PartitionGroup partitionGroup) {
        super(partitionGroup);
    }

    @Override
    public int type() {
        return NsrCommandType.NSR_REMOVE_PARTITIONGROUP;
    }
}
