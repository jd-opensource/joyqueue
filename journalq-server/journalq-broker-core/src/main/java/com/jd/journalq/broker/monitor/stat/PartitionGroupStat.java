package com.jd.journalq.broker.monitor.stat;

import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.concurrent.ConcurrentMap;

/**
 * PartitionGroupStat
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/11
 */
public class PartitionGroupStat implements Serializable {

    private String topic;
    private String app;
    private int partitionGroup;

    private EnQueueStat enQueueStat = new EnQueueStat();
    private DeQueueStat deQueueStat = new DeQueueStat();
    private ReplicationStat replicationStat = new ReplicationStat();

    private ConcurrentMap<Short /** partition **/, PartitionStat> partitionStatMap = Maps.newConcurrentMap();

    public PartitionGroupStat(String topic, String app, int partitionGroup) {
        this.topic = topic;
        this.app = app;
        this.partitionGroup = partitionGroup;
    }

    public PartitionStat getOrCreatePartitionStat(short partition) {
        PartitionStat partitionStat = partitionStatMap.get(partition);
        if (partitionStat == null) {
            partitionStatMap.putIfAbsent(partition, new PartitionStat(topic, app, partition));
            partitionStat = partitionStatMap.get(partition);
        }
        return partitionStat;
    }

    public String getTopic() {
        return topic;
    }

    public String getApp() {
        return app;
    }

    public EnQueueStat getEnQueueStat() {
        return enQueueStat;
    }

    public DeQueueStat getDeQueueStat() {
        return deQueueStat;
    }

    public int getPartitionGroup() {
        return partitionGroup;
    }

    public ConcurrentMap<Short, PartitionStat> getPartitionStatMap() {
        return partitionStatMap;
    }

    public ReplicationStat getReplicationStat() {
        return replicationStat;
    }
}
