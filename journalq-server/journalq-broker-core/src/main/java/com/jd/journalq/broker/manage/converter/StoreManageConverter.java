package com.jd.journalq.broker.manage.converter;

import com.jd.journalq.manage.IndexItem;
import com.jd.journalq.manage.PartitionGroupMetric;
import com.jd.journalq.manage.PartitionMetric;
import com.jd.journalq.manage.TopicMetric;
import com.jd.journalq.store.StoreManagementService;

import java.util.Arrays;

public class StoreManageConverter {

    public static TopicMetric[] convert(StoreManagementService.TopicMetric[] topicMetrics) {
        TopicMetric[] result = new TopicMetric[topicMetrics.length];
        for (int i = 0; i < topicMetrics.length; i++) {
            result[i] = convert(topicMetrics[i]);
        }
        return result;
    }

    public static TopicMetric convert(StoreManagementService.TopicMetric topicMetric) {
        TopicMetric result = new TopicMetric();
        result.setTopic(topicMetric.getTopic());
        result.setPartitionGroupMetrics(convert(topicMetric.getPartitionGroupMetrics()));
        return result;
    }

    public static PartitionGroupMetric[] convert(StoreManagementService.PartitionGroupMetric[] partitionGroupMetrics) {
        PartitionGroupMetric[] result = new PartitionGroupMetric[partitionGroupMetrics.length];
        for (int i = 0; i < partitionGroupMetrics.length; i++) {
            result[i] = convert(partitionGroupMetrics[i]);
        }
        return result;
    }

    public static PartitionGroupMetric convert(StoreManagementService.PartitionGroupMetric partitionGroupMetric) {
        PartitionGroupMetric result = new PartitionGroupMetric();
        result.setPartitionGroup(partitionGroupMetric.getPartitionGroup());
        result.setPartitionMetrics(convert(partitionGroupMetric.getPartitionMetrics()));
        result.setLeftPosition(partitionGroupMetric.getLeftPosition());
        result.setRightPosition(partitionGroupMetric.getRightPosition());
        result.setIndexPosition(partitionGroupMetric.getIndexPosition());
        result.setFlushPosition(partitionGroupMetric.getFlushPosition());
        result.setReplicationPosition(partitionGroupMetric.getReplicationPosition());
        return result;
    }

    public static PartitionMetric[] convert(StoreManagementService.PartitionMetric[] partitionMetrics) {
        PartitionMetric[] result = new PartitionMetric[partitionMetrics.length];
        for (int i = 0; i < partitionMetrics.length; i++) {
            result[i] = convert(partitionMetrics[i]);
        }
        return result;
    }

    public static PartitionMetric convert(StoreManagementService.PartitionMetric partitionMetric) {
        PartitionMetric result = new PartitionMetric();
        result.setPartition(partitionMetric.getPartition());
        result.setLeftIndex(partitionMetric.getLeftIndex());
        result.setRightIndex(partitionMetric.getRightIndex());
        return result;
    }

    public static IndexItem [] convert(Long [] indexItems) {

        return null == indexItems ? null : Arrays.stream(indexItems).map(i -> new IndexItem(i, 0)).toArray(IndexItem[]::new);
    }
}