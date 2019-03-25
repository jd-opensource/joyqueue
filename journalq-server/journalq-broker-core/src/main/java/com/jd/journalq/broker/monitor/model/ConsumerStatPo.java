package com.jd.journalq.broker.monitor.model;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * consumerstat po
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/12
 */
public class ConsumerStatPo extends BasePo {

    private RetryStatPo retryStat;
    private DeQueueStatPo deQueueStat;
    private Map<Integer, PartitionGroupStatPo> partitionGroupStatMap = Maps.newHashMap();

    public ConsumerStatPo(RetryStatPo retryStat, DeQueueStatPo deQueueStat) {
        this.retryStat = retryStat;
        this.deQueueStat = deQueueStat;
    }

    public ConsumerStatPo() {

    }

    public void setRetryStat(RetryStatPo retryStat) {
        this.retryStat = retryStat;
    }

    public RetryStatPo getRetryStat() {
        return retryStat;
    }

    public DeQueueStatPo getDeQueueStat() {
        return deQueueStat;
    }

    public void setDeQueueStat(DeQueueStatPo deQueueStat) {
        this.deQueueStat = deQueueStat;
    }

    public void setPartitionGroupStatMap(Map<Integer, PartitionGroupStatPo> partitionGroupStatMap) {
        this.partitionGroupStatMap = partitionGroupStatMap;
    }

    public Map<Integer, PartitionGroupStatPo> getPartitionGroupStatMap() {
        return partitionGroupStatMap;
    }
}