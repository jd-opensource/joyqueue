package com.jd.journalq.common.manage;

import java.io.Serializable;

public class PartitionGroupMetric implements Serializable {

    private int partitionGroup;
    private PartitionMetric[] partitionMetrics;
    private long leftPosition;
    private long rightPosition;
    private long indexPosition;
    private long flushPosition;
    private long replicationPosition;
    private String  partitions;

    public int getPartitionGroup() {
        return partitionGroup;
    }

    public void setPartitionGroup(int partitionGroup) {
        this.partitionGroup = partitionGroup;
    }

    public PartitionMetric[] getPartitionMetrics() {
        return partitionMetrics;
    }

    public void setPartitionMetrics(PartitionMetric[] partitionMetrics) {
        this.partitionMetrics = partitionMetrics;
    }

    public long getLeftPosition() {
        return leftPosition;
    }

    public void setLeftPosition(long leftPosition) {
        this.leftPosition = leftPosition;
    }

    public long getRightPosition() {
        return rightPosition;
    }

    public void setRightPosition(long rightPosition) {
        this.rightPosition = rightPosition;
    }

    public long getIndexPosition() {
        return indexPosition;
    }

    public void setIndexPosition(long indexPosition) {
        this.indexPosition = indexPosition;
    }

    public long getFlushPosition() {
        return flushPosition;
    }

    public void setFlushPosition(long flushPosition) {
        this.flushPosition = flushPosition;
    }

    public long getReplicationPosition() {
        return replicationPosition;
    }

    public void setReplicationPosition(long replicationPosition) {
        this.replicationPosition = replicationPosition;
    }

    public String getPartitions() {
        return partitions;
    }

    public void setPartitions(String partitions) {
        this.partitions = partitions;
    }
}