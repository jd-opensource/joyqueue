package io.chubao.joyqueue.monitor;

/**
 * 生产者信息
 *
 * author: gaohaoxiang
 * date: 2018/10/10
 */
public class ProducerMonitorInfo extends BaseMonitorInfo {

    private String topic;
    private String app;
    private int connections;

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

    public int getConnections() {
        return connections;
    }

    public void setConnections(int connections) {
        this.connections = connections;
    }

    public EnQueueMonitorInfo getEnQueue() {
        return enQueue;
    }

    public void setEnQueue(EnQueueMonitorInfo enQueue) {
        this.enQueue = enQueue;
    }
}