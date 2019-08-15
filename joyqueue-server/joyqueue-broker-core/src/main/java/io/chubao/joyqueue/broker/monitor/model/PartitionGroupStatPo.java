package io.chubao.joyqueue.broker.monitor.model;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * partitiongroupstat po
 *
 * author: gaohaoxiang
 * date: 2018/10/12
 */
public class PartitionGroupStatPo extends BasePo {

    private EnQueueStatPo enQueueStat;
    private DeQueueStatPo deQueueStat;
    private Map<Short, PartitionStatPo> partitionStatMap = Maps.newHashMap();
    private ReplicationStatPo replicationStat;

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

    public Map<Short, PartitionStatPo> getPartitionStatMap() {
        return partitionStatMap;
    }

    public void setPartitionStatMap(Map<Short, PartitionStatPo> partitionStatMap) {
        this.partitionStatMap = partitionStatMap;
    }

    public void setReplicationStat(ReplicationStatPo replicationStat) {
        this.replicationStat = replicationStat;
    }

    public ReplicationStatPo getReplicationStat() {
        return replicationStat;
    }
}