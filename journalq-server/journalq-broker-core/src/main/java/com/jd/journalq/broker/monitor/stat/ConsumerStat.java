package com.jd.journalq.broker.monitor.stat;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * ConsumerStat
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/11
 */
public class ConsumerStat {

    private String topic;
    private String app;
    private DeQueueStat deQueueStat = new DeQueueStat();
    private RetryStat retryStat = new RetryStat();
    private ConnectionStat connectionStat = new ConnectionStat();
    private ConcurrentMap<Integer /** partitionGroupId **/, PartitionGroupStat> partitionGroupStatMap = Maps.newConcurrentMap();

    public ConsumerStat(String topic, String app) {
        this.topic = topic;
        this.app = app;
    }

    public PartitionStat getPartitionStat(short partition) {
        for (Map.Entry<Integer, PartitionGroupStat> entry : partitionGroupStatMap.entrySet()) {
            PartitionStat partitionStat = entry.getValue().getPartitionStatMap().get(partition);
            if (partitionStat != null) {
                return partitionStat;
            }
        }
        return new PartitionStat(topic, app, partition);
    }

    public PartitionGroupStat getOrCreatePartitionGroupStat(int partitionGroup) {
        PartitionGroupStat partitionGroupStat = partitionGroupStatMap.get(partitionGroup);
        if (partitionGroupStat == null) {
            partitionGroupStatMap.putIfAbsent(partitionGroup, new PartitionGroupStat(topic, app, partitionGroup));
            partitionGroupStat = partitionGroupStatMap.get(partitionGroup);
        }
        return partitionGroupStat;
    }

    public String getTopic() {
        return topic;
    }

    public String getApp() {
        return app;
    }

    public DeQueueStat getDeQueueStat() {
        return deQueueStat;
    }

    public RetryStat getRetryStat() {
        return retryStat;
    }

    public ConnectionStat getConnectionStat() {
        return connectionStat;
    }

    public ConcurrentMap<Integer, PartitionGroupStat> getPartitionGroupStatMap() {
        return partitionGroupStatMap;
    }
}