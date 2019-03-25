package com.jd.journalq.client.internal.producer.support;

import com.jd.journalq.client.internal.metadata.domain.PartitionMetadata;
import com.jd.journalq.client.internal.metadata.domain.TopicMetadata;
import com.jd.journalq.client.internal.producer.PartitionSelector;
import com.jd.journalq.client.internal.producer.domain.ProduceMessage;

import java.util.List;

/**
 * AbstractPartitionSelector
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/10
 */
public abstract class AbstractPartitionSelector implements PartitionSelector {

    @Override
    public PartitionMetadata select(ProduceMessage message, TopicMetadata topicMetadata, List<PartitionMetadata> partitions) {
        if (message.getPartition() != ProduceMessage.NONE_PARTITION) {
            return selectPartition(message, topicMetadata, partitions);
        }
        if (message.getPartitionKey() != ProduceMessage.NONE_PARTITION_KEY) {
            return hashPartition(message, topicMetadata, partitions);
        }
        return nextPartition(message, topicMetadata, partitions);
    }

    protected PartitionMetadata selectPartition(ProduceMessage message, TopicMetadata topicMetadata, List<PartitionMetadata> partitions) {
        for (PartitionMetadata partition : partitions) {
            if (message.getPartition() == partition.getId()) {
                return partition;
            }
        }
        return null;
    }

    protected PartitionMetadata hashPartition(ProduceMessage message, TopicMetadata topicMetadata, List<PartitionMetadata> partitions) {
        return partitions.get(Math.abs(message.getPartitionKey().hashCode() % partitions.size()));
    }

    protected abstract PartitionMetadata nextPartition(ProduceMessage message, TopicMetadata topicMetadata, List<PartitionMetadata> partitions);
}