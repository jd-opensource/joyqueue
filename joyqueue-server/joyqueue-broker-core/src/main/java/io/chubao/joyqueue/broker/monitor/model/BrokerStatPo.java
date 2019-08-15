package io.chubao.joyqueue.broker.monitor.model;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * brokerstat
 *
 * author: gaohaoxiang
 * date: 2018/10/12
 */
public class BrokerStatPo extends BasePo {

    private Integer brokerId;
    private int version;
    private DeQueueStatPo deQueueStat;
    private EnQueueStatPo enQueueStat;
    private Map<String, TopicStatPo> topicStatMap = Maps.newHashMap();
    private ReplicationStatPo replicationStat;

    public void setBrokerId(Integer brokerId) {
        this.brokerId = brokerId;
    }

    public Integer getBrokerId() {
        return brokerId;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

    public DeQueueStatPo getDeQueueStat() {
        return deQueueStat;
    }

    public void setDeQueueStat(DeQueueStatPo deQueueStat) {
        this.deQueueStat = deQueueStat;
    }

    public EnQueueStatPo getEnQueueStat() {
        return enQueueStat;
    }

    public void setEnQueueStat(EnQueueStatPo enQueueStat) {
        this.enQueueStat = enQueueStat;
    }

    public Map<String, TopicStatPo> getTopicStatMap() {
        return topicStatMap;
    }

    public void setTopicStatMap(Map<String, TopicStatPo> topicStatMap) {
        this.topicStatMap = topicStatMap;
    }

    public void setReplicationStat(ReplicationStatPo replicationStat) {
        this.replicationStat = replicationStat;
    }

    public ReplicationStatPo getReplicationStat() {
        return replicationStat;
    }
}