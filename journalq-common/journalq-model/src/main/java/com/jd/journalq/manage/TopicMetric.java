package com.jd.journalq.manage;

import java.io.Serializable;

public class TopicMetric implements Serializable {
    private String topic;
    private PartitionGroupMetric[] partitionGroupMetrics;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public PartitionGroupMetric[] getPartitionGroupMetrics() {
        return partitionGroupMetrics;
    }

    public void setPartitionGroupMetrics(PartitionGroupMetric[] partitionGroupMetrics) {
        this.partitionGroupMetrics = partitionGroupMetrics;
    }
}