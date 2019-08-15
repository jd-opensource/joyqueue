package io.chubao.joyqueue.broker.monitor.stat;

import java.io.Serializable;

/**
 * PartitionStat
 *
 * author: gaohaoxiang
 * date: 2018/10/11
 */
public class PartitionStat implements Serializable {

    private String topic;
    private String app;
    private short partition;

    private EnQueueStat enQueueStat = new EnQueueStat();
    private DeQueueStat deQueueStat = new DeQueueStat();

    public PartitionStat(String topic, String app, short partition) {
        this.topic = topic;
        this.app = app;
        this.partition = partition;
    }

    public String getTopic() {
        return topic;
    }

    public String getApp() {
        return app;
    }

    public short getPartition() {
        return partition;
    }

    public EnQueueStat getEnQueueStat() {
        return enQueueStat;
    }

    public DeQueueStat getDeQueueStat() {
        return deQueueStat;
    }
}
