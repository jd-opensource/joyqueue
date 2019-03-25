package com.jd.journalq.broker.monitor.stat;

import com.jd.journalq.broker.monitor.PendingStat;

import java.util.HashMap;
import java.util.Map;

public class ConsumerPendingStat implements PendingStat<Integer,PartitionGroupPendingStat> {
    private String topic;
    private String app;
    private long pending;
    private Map<Integer/*partitionGroup*/,PartitionGroupPendingStat> partitionGroupPendingStatMap =new HashMap<>();

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

    public long getPending() {
        return pending;
    }

    public void setPending(long pending) {
        this.pending = pending;
    }

    @Override
    public void setPendingStatSubMap(Map<Integer, PartitionGroupPendingStat> subMap) {
        this.partitionGroupPendingStatMap=subMap;
    }

    @Override
    public Map<Integer, PartitionGroupPendingStat> getPendingStatSubMap() {
        return partitionGroupPendingStatMap;
    }
}
