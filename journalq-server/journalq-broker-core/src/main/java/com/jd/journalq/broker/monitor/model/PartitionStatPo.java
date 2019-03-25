package com.jd.journalq.broker.monitor.model;

/**
 * partitionstat po
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/12
 */
public class PartitionStatPo extends BasePo {

    private short partition;
    private EnQueueStatPo enQueueStat;
    private DeQueueStatPo deQueueStat;

    public short getPartition() {
        return partition;
    }

    public void setPartition(short partition) {
        this.partition = partition;
    }

    public EnQueueStatPo getEnQueueStat() {
        return enQueueStat;
    }

    public void setEnQueueStat(EnQueueStatPo enQueueStat) {
        this.enQueueStat = enQueueStat;
    }

    public DeQueueStatPo getDeQueueStat() {
        return deQueueStat;
    }

    public void setDeQueueStat(DeQueueStatPo deQueueStat) {
        this.deQueueStat = deQueueStat;
    }
}