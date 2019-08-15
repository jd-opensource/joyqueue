package io.chubao.joyqueue.monitor;

/**
 * 生产者分区组信息
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/10
 */
public class ProducerPartitionGroupMonitorInfo extends BaseMonitorInfo {

    private String topic;
    private String app;
    private int partitionGroupId;

    private EnQueueMonitorInfo enQueue;

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

    public EnQueueMonitorInfo getEnQueue() {
        return enQueue;
    }

    public void setEnQueue(EnQueueMonitorInfo enQueue) {
        this.enQueue = enQueue;
    }
}