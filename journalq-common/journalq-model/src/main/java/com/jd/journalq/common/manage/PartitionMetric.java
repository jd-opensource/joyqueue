package com.jd.journalq.common.manage;

import java.io.Serializable;

public class PartitionMetric implements Serializable {
    private short partition;
    private long leftIndex;
    private long rightIndex;

    public short getPartition() {
        return partition;
    }

    public void setPartition(short partition) {
        this.partition = partition;
    }

    public long getLeftIndex() {
        return leftIndex;
    }

    public void setLeftIndex(long leftIndex) {
        this.leftIndex = leftIndex;
    }

    public long getRightIndex() {
        return rightIndex;
    }

    public void setRightIndex(long rightIndex) {
        this.rightIndex = rightIndex;
    }
}