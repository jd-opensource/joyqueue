package com.jd.journalq.common.monitor;

/**
 * PartitionAckMonitorInfo
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/30
 */
public class PartitionAckMonitorInfo extends BaseMonitorInfo {

    private short partition;
    private long index;
    private long leftIndex;
    private long rightIndex;

    public PartitionAckMonitorInfo() {

    }

    public PartitionAckMonitorInfo(short partition, long index, long leftIndex, long rightIndex) {
        this.partition = partition;
        this.index = index;
        this.leftIndex = leftIndex;
        this.rightIndex = rightIndex;
    }

    public short getPartition() {
        return partition;
    }

    public void setPartition(short partition) {
        this.partition = partition;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public void setLeftIndex(long leftIndex) {
        this.leftIndex = leftIndex;
    }

    public long getLeftIndex() {
        return leftIndex;
    }

    public void setRightIndex(long rightIndex) {
        this.rightIndex = rightIndex;
    }

    public long getRightIndex() {
        return rightIndex;
    }
}