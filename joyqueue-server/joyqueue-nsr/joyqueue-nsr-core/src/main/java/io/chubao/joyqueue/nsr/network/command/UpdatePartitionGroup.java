package io.chubao.joyqueue.nsr.network.command;

import io.chubao.joyqueue.domain.PartitionGroup;
import io.chubao.joyqueue.network.transport.command.Types;

public class UpdatePartitionGroup extends OperatePartitionGroup implements Types {
    public UpdatePartitionGroup(PartitionGroup partitionGroup, boolean rollback) {
        super(partitionGroup, rollback);
    }

    public UpdatePartitionGroup(PartitionGroup partitionGroup) {
        super(partitionGroup);
    }

    @Override
    public int type() {
        return NsrCommandType.NSR_UPDATE_PARTITIONGROUP;
    }

    @Override
    public String toString() {
        return "UpdatePartitionGroup{" +
                "rollback=" + rollback +
                ", partitionGroup=" + partitionGroup +
                ", type=" + type() +
                '}';
    }

    @Override
    public int[] types() {
        return new int[]{type(),NsrCommandType.NSR_LEADERCHANAGE_PARTITIONGROUP};
    }
}
