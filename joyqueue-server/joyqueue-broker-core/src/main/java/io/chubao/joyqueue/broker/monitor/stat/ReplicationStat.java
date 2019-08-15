package io.chubao.joyqueue.broker.monitor.stat;

import java.io.Serializable;

/**
 * ReplicationStat
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/16
 */
public class ReplicationStat implements Serializable {

    private String topic;
    private int partitionGroup;

    private EnQueueStat replicaStat = new EnQueueStat();
    private EnQueueStat appendStat = new EnQueueStat();

    public ReplicationStat() {
    }

    public ReplicationStat(String topic, int partitionGroup) {
        this.topic = topic;
        this.partitionGroup = partitionGroup;
    }

    public String getTopic() {
        return topic;
    }

    public int getPartitionGroup() {
        return partitionGroup;
    }

    public EnQueueStat getReplicaStat() {
        return replicaStat;
    }

    public EnQueueStat getAppendStat() {
        return appendStat;
    }
}