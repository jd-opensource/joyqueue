package com.jd.journalq.broker.jmq.coordinator.assignment.domain;

/**
 * PartitionGroupAssignmentMetadata
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/5
 */
public class PartitionGroupAssignmentMetadata {

    private int partitionGroupId;
    private int assigned;

    public int incrAssigned() {
        return assigned++;
    }

    public int decrAssigned() {
        return assigned--;
    }

    public int getPartitionGroupId() {
        return partitionGroupId;
    }

    public void setPartitionGroupId(int partitionGroupId) {
        this.partitionGroupId = partitionGroupId;
    }

    public void setAssigned(int assigned) {
        this.assigned = assigned;
    }

    public int getAssigned() {
        return assigned;
    }
}