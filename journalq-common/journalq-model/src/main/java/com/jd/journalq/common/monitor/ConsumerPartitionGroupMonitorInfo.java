package com.jd.journalq.common.monitor;

/**
 * 消费者分区组信息
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/10
 */
public class ConsumerPartitionGroupMonitorInfo extends BaseMonitorInfo {

    private String topic;
    private String app;
    private int partitionGroupId;

    private DeQueueMonitorInfo deQueue;
    private PendingMonitorInfo pending;

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

    public void setPartitionGroupId(int partitionGroupId) {
        this.partitionGroupId = partitionGroupId;
    }

    public int getPartitionGroupId() {
        return partitionGroupId;
    }

    public DeQueueMonitorInfo getDeQueue() {
        return deQueue;
    }

    public void setDeQueue(DeQueueMonitorInfo deQueue) {
        this.deQueue = deQueue;
    }

    public PendingMonitorInfo getPending() {
        return pending;
    }

    public void setPending(PendingMonitorInfo pending) {
        this.pending = pending;
    }
}