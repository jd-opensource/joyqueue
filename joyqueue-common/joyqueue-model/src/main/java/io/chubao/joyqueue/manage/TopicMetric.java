package io.chubao.joyqueue.manage;

import java.io.Serializable;

public class TopicMetric implements Serializable {
    private static final long serialVersionUID = 1L;
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