package com.jd.journalq.manage;

import java.io.Serializable;

/**
 * Created by wangxiaofei1 on 2019/4/10.
 */
public class PartitionPosition implements Serializable {
    private int partition;
    private long rightPosition;
    private long rightPositionInterval;

    public int getPartition() {
        return partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public long getRightPosition() {
        return rightPosition;
    }

    public void setRightPosition(long rightPosition) {
        this.rightPosition = rightPosition;
    }

    public long getRightPositionInterval() {
        return rightPositionInterval;
    }

    public void setRightPositionInterval(long rightPositionInterval) {
        this.rightPositionInterval = rightPositionInterval;
    }
}
