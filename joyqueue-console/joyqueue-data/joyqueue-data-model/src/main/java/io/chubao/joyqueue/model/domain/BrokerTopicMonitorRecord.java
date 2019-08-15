package io.chubao.joyqueue.model.domain;

/**
 * Created by wangxiaofei1 on 2019/3/13.
 */
public class BrokerTopicMonitorRecord {
    private String app;
    private String topic;
    private long connections;
    private long count;
    private long totalSize;

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public long getConnections() {
        return connections;
    }

    public void setConnections(long connections) {
        this.connections = connections;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }
}
