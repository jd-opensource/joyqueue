package com.jd.journalq.broker.monitor.model;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * 主题po
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/12
 */
public class TopicStatPo extends BasePo {

    private EnQueueStatPo enQueueStat;
    private DeQueueStatPo deQueueStat;
    private Map<String, AppStatPo> appStatMap = Maps.newHashMap();
    private Map<Integer, PartitionGroupStatPo> partitionGroupStatMap = Maps.newHashMap();

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

    public Map<String, AppStatPo> getAppStatMap() {
        return appStatMap;
    }

    public void setAppStatMap(Map<String, AppStatPo> appStatMap) {
        this.appStatMap = appStatMap;
    }

    public void setPartitionGroupStatMap(Map<Integer, PartitionGroupStatPo> partitionGroupStatMap) {
        this.partitionGroupStatMap = partitionGroupStatMap;
    }

    public Map<Integer, PartitionGroupStatPo> getPartitionGroupStatMap() {
        return partitionGroupStatMap;
    }
}