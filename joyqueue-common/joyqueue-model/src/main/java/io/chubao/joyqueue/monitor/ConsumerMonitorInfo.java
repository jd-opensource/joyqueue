package io.chubao.joyqueue.monitor;

/**
 * 消费者信息
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/10
 */
public class ConsumerMonitorInfo extends BaseMonitorInfo {

    private String topic;
    private String app;
    private int connections;

    private DeQueueMonitorInfo deQueue;
    private RetryMonitorInfo retry;
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

    public int getConnections() {
        return connections;
    }

    public void setConnections(int connections) {
        this.connections = connections;
    }

    public DeQueueMonitorInfo getDeQueue() {
        return deQueue;
    }

    public void setDeQueue(DeQueueMonitorInfo deQueue) {
        this.deQueue = deQueue;
    }

    public RetryMonitorInfo getRetry() {
        return retry;
    }

    public void setRetry(RetryMonitorInfo retry) {
        this.retry = retry;
    }

    public PendingMonitorInfo getPending() {
        return pending;
    }

    public void setPending(PendingMonitorInfo pending) {
        this.pending = pending;
    }
}