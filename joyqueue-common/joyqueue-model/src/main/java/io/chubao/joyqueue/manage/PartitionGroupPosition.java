package io.chubao.joyqueue.manage;

import java.io.Serializable;
import java.util.List;

/**
 * Created by wangxiaofei1 on 2019/4/10.
 */
public class PartitionGroupPosition implements Serializable {
    private boolean leader;
    private int partitionGroup;
    private long rightPosition;
    private long rightPositionInterval;
    private List<PartitionPosition> partitionPositionList;
    private String brokerId;

    public boolean isLeader() {
        return leader;
    }

    public void setLeader(boolean leader) {
        this.leader = leader;
    }

    public void setBrokerId(String brokerId) {
        this.brokerId = brokerId;
    }

    public String getBrokerId() {
        return brokerId;
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
