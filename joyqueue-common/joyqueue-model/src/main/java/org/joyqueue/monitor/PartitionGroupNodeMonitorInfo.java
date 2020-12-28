package org.joyqueue.monitor;

/**
 * PartitionGroupReplicationNodeMonitorInfo
 * author: gaohaoxiang
 * date: 2020/11/23
 */
public class PartitionGroupNodeMonitorInfo {

    private int replicaId;
    private long rightPosition;
    private long commitPosition;
    private long pending;
    private long lastAppendTime;
    private long lastAppendSuccessTime;
    private long lastReplicateConsumePosTime;

    public int getReplicaId() {
        return replicaId;
    }

    public void setReplicaId(int replicaId) {
        this.replicaId = replicaId;
    }

    public long getRightPosition() {
        return rightPosition;
    }

    public void setRightPosition(long rightPosition) {
        this.rightPosition = rightPosition;
    }

    public long getCommitPosition() {
        return commitPosition;
    }

    public void setCommitPosition(long commitPosition) {
        this.commitPosition = commitPosition;
    }

    public long getPending() {
        return pending;
    }

    public void setPending(long pending) {
        this.pending = pending;
    }

    public long getLastAppendTime() {
        return lastAppendTime;
    }

    public void setLastAppendTime(long lastAppendTime) {
        this.lastAppendTime = lastAppendTime;
    }

    public long getLastAppendSuccessTime() {
        return lastAppendSuccessTime;
    }

    public void setLastAppendSuccessTime(long lastAppendSuccessTime) {
        this.lastAppendSuccessTime = lastAppendSuccessTime;
    }

    public long getLastReplicateConsumePosTime() {
        return lastReplicateConsumePosTime;
    }

    public void setLastReplicateConsumePosTime(long lastReplicateConsumePosTime) {
        this.lastReplicateConsumePosTime = lastReplicateConsumePosTime;
    }
}