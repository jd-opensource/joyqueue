package com.jd.journalq.broker.monitor.stat;

import com.jd.journalq.broker.monitor.PendingStat;

import java.util.HashMap;
import java.util.Map;

public class PartitionGroupPendingStat implements PendingStat<Short,Long> {
    private String topic;
    private String app;
    private int  partitionGroup;
    private long pending;
    private Map<Short/*partition*/,Long/*pending*/> partitionPendingStatMap =new HashMap<>();

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

    public int getPartitionGroup() {
        return partitionGroup;
    }

    public void setPartitionGroup(int partitionGroup) {
        this.partitionGroup = partitionGroup;
    }

    public long getPending() {
        return pending;
    }

    public void setPending(long pending) {
        this.pending = pending;
    }

    @Override
    public void setPendingStatSubMap(Map<Short, Long> subMap) {
        this.partitionPendingStatMap=subMap;
    }

    @Override
    public Map<Short, Long> getPendingStatSubMap() {
        return partitionPendingStatMap;
    }
}
