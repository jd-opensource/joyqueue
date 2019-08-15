package io.chubao.joyqueue.client.internal.consumer.coordinator.domain;

/**
 * PartitionAssignmentHolder
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/11
 */
public class PartitionAssignmentHolder {

    private PartitionAssignment partitionAssignment;
    private long createTime;

    public PartitionAssignmentHolder(PartitionAssignment partitionAssignment, long createTime) {
        this.partitionAssignment = partitionAssignment;
        this.createTime = createTime;
    }

    public PartitionAssignment getPartitionAssignment() {
        return partitionAssignment;
    }

    public void setPartitionAssignment(PartitionAssignment partitionAssignment) {
        this.partitionAssignment = partitionAssignment;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
}