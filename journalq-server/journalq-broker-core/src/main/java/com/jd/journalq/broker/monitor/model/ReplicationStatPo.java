package com.jd.journalq.broker.monitor.model;

/**
 * ReplicationStatPo
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/16
 */
public class ReplicationStatPo extends BasePo {

    private EnQueueStatPo replicaStat;
    private EnQueueStatPo appendStat;

    public ReplicationStatPo() {

    }

    public ReplicationStatPo(EnQueueStatPo replicaStat, EnQueueStatPo appendStat) {
        this.replicaStat = replicaStat;
        this.appendStat = appendStat;
    }

    public EnQueueStatPo getReplicaStat() {
        return replicaStat;
    }

    public void setReplicaStat(EnQueueStatPo replicaStat) {
        this.replicaStat = replicaStat;
    }

    public EnQueueStatPo getAppendStat() {
        return appendStat;
    }

    public void setAppendStat(EnQueueStatPo appendStat) {
        this.appendStat = appendStat;
    }
}