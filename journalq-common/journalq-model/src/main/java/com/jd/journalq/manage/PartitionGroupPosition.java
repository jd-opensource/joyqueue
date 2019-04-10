package com.jd.journalq.manage;

import java.io.Serializable;
import java.util.List;

/**
 * Created by wangxiaofei1 on 2019/4/10.
 */
public class PartitionGroupPosition implements Serializable {
    private boolean isMaster;
    private int partitionGroup;
    private long rightPosition;
    private long rightPositionInterval;
    private List<PartitionPosition> partitionPositionList;

    public boolean isMaster() {
        return isMaster;
    }

    public void setMaster(boolean master) {
        isMaster = master;
    }

    public int getPartitionGroup() {
        return partitionGroup;
    }

    public void setPartitionGroup(int partitionGroup) {
        this.partitionGroup = partitionGroup;
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

    public List<PartitionPosition> getPartitionPositionList() {
        return partitionPositionList;
    }

    public void setPartitionPositionList(List<PartitionPosition> partitionPositionList) {
        this.partitionPositionList = partitionPositionList;
    }
}
