package io.chubao.joyqueue.nsr.network.command;

import io.chubao.joyqueue.domain.PartitionGroup;
import io.chubao.joyqueue.network.transport.command.JoyQueuePayload;

public abstract class OperatePartitionGroup extends JoyQueuePayload {
    protected boolean rollback = false;
    protected PartitionGroup partitionGroup;

    public OperatePartitionGroup(PartitionGroup partitionGroup) {
        this.partitionGroup = partitionGroup;
    }

    public OperatePartitionGroup(PartitionGroup partitionGroup, boolean rollback) {
        this.rollback = rollback;
        this.partitionGroup = partitionGroup;
    }

    public OperatePartitionGroup rollback(boolean rollback) {
        this.rollback = rollback;
        return this;
    }

    public boolean isRollback() {
        return rollback;
    }


    public PartitionGroup getPartitionGroup() {
        return partitionGroup;
    }

    public void setPartitionGroup(PartitionGroup partitionGroup) {
        this.partitionGroup = partitionGroup;
    }

    @Override
    public String toString() {
        return "OperatePartitionGroup{" +
                "rollback=" + rollback +
                ", partitionGroup=" + partitionGroup +
                '}';
    }
}
