package io.chubao.joyqueue.monitor;

/**
 * 分区信息
 * @author lining11
 * Date: 2018/9/13
 */
public class PartitionMonitorInfo extends BaseMonitorInfo {

    private String topic;
    private String app;
    private short partition;

    private EnQueueMonitorInfo enQueue;
    private DeQueueMonitorInfo deQueue;

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

    public short getPartition() {
        return partition;
    }

    public void setPartition(short partition) {
        this.partition = partition;
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
}