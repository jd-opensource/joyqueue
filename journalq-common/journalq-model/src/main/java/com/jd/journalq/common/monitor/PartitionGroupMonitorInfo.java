package com.jd.journalq.common.monitor;

/**
 * 分区组信息
 * @author lining11
 * Date: 2018/9/13
 */
public class PartitionGroupMonitorInfo extends BaseMonitorInfo {

    private String topic;
    private String app;
    private int partitionGroup;
    private long leftPosition;
    private long rightPosition;
    private long indexPosition;
    private long flushPosition;
    private long replicationPosition;

    private EnQueueMonitorInfo enQueue;
    private DeQueueMonitorInfo deQueue;
    private ReplicationMonitorInfo replication;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public int getPartitionGroup() {
        return partitionGroup;
    }

    public void setPartitionGroup(int partitionGroup) {
        this.partitionGroup = partitionGroup;
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

    public EnQueueMonitorInfo getEnQueue() {
        return enQueue;
    }

    public void setEnQueue(EnQueueMonitorInfo enQueue) {
        this.enQueue = enQueue;
    }

    public DeQueueMonitorInfo getDeQueue() {
        return deQueue;
    }

    public void setDeQueue(DeQueueMonitorInfo deQueue) {
        this.deQueue = deQueue;
    }

    public void setReplication(ReplicationMonitorInfo replication) {
        this.replication = replication;
    }

    public ReplicationMonitorInfo getReplication() {
        return replication;
    }
}