package io.chubao.joyqueue.model.domain;

import io.chubao.joyqueue.monitor.DeQueueMonitorInfo;
import io.chubao.joyqueue.monitor.EnQueueMonitorInfo;
import io.chubao.joyqueue.monitor.PendingMonitorInfo;
import io.chubao.joyqueue.monitor.RetryMonitorInfo;


/**
 * monitor info
 *
 **/
public class BrokerMonitorRecord {
    private String app;
    private String topic;
    private String ip;
    private int  partitionGroup;
    private int  partition;
    private long connections;
    private PendingMonitorInfo pending;
    private DeQueueMonitorInfo deQuence;
    private EnQueueMonitorInfo enQuence;
    private RetryMonitorInfo retry;

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

    public PendingMonitorInfo getPending() {
        return pending;
    }

    public void setPending(PendingMonitorInfo pending) {
        this.pending = pending;
    }

    public DeQueueMonitorInfo getDeQuence() {
        return deQuence;
    }

    public void setDeQuence(DeQueueMonitorInfo deQuence) {
        this.deQuence = deQuence;
    }

    public EnQueueMonitorInfo getEnQuence() {
        return enQuence;
    }

    public void setEnQuence(EnQueueMonitorInfo enQuence) {
        this.enQuence = enQuence;
    }

    public RetryMonitorInfo getRetry() {
        return retry;
    }

    public void setRetry(RetryMonitorInfo retry) {
        this.retry = retry;
    }

    public int getPartition() {
        return partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPartitionGroup() {
        return partitionGroup;
    }

    public void setPartitionGroup(int partitionGroup) {
        this.partitionGroup = partitionGroup;
    }
}
