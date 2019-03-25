package com.jd.journalq.monitor;

/**
 * 主题信息
 * @author lining11
 * Date: 2018/9/13
 */
public class TopicMonitorInfo extends BaseMonitorInfo {

    private String topic;

    private ConnectionMonitorInfo connection;
    private EnQueueMonitorInfo enQueue;
    private DeQueueMonitorInfo deQueue;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setConnection(ConnectionMonitorInfo connection) {
        this.connection = connection;
    }

    public ConnectionMonitorInfo getConnection() {
        return connection;
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