package org.joyqueue.server.retry.model;

/**
 * Retry monitor item
 *
 **/
public class RetryMonitorItem {
    private String topic;
    private String app;
    private long minSendTime;
    private long maxSendTime;
    private int  count;
    private short status;
    private int  order;
    private boolean existSubscribe;

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

    public long getMinSendTime() {
        return minSendTime;
    }

    public void setMinSendTime(long minSendTime) {
        this.minSendTime = minSendTime;
    }

    public long getMaxSendTime() {
        return maxSendTime;
    }

    public void setMaxSendTime(long maxSendTime) {
        this.maxSendTime = maxSendTime;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public short getStatus() {
        return status;
    }

    public void setStatus(short status) {
        this.status = status;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public boolean isExistSubscribe() {
        return existSubscribe;
    }

    public void setExistSubscribe(boolean existSubscribe) {
        this.existSubscribe = existSubscribe;
    }
}
