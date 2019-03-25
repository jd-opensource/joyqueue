package com.jd.journalq.model.domain;

import com.jd.journalq.common.manage.PartitionGroupMetric;

import java.util.List;

/**
 * Created by wangxiaofei1 on 2019/3/13.
 */
public class BrokerTopicMonitor {
    private String topic;
    private List<BrokerTopicMonitorRecord> brokerTopicMonitorRecordList;
    private List<PartitionGroupMetric> partitionGroupMetricList;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public List<BrokerTopicMonitorRecord> getBrokerTopicMonitorRecordList() {
        return brokerTopicMonitorRecordList;
    }

    public void setBrokerTopicMonitorRecordList(List<BrokerTopicMonitorRecord> brokerTopicMonitorRecordList) {
        this.brokerTopicMonitorRecordList = brokerTopicMonitorRecordList;
    }

    public List<PartitionGroupMetric> getPartitionGroupMetricList() {
        return partitionGroupMetricList;
    }

    public void setPartitionGroupMetricList(List<PartitionGroupMetric> partitionGroupMetricList) {
        this.partitionGroupMetricList = partitionGroupMetricList;
    }
}
