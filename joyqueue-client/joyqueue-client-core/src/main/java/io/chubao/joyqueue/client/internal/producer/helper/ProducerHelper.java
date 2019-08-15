package io.chubao.joyqueue.client.internal.producer.helper;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.client.internal.metadata.domain.PartitionMetadata;
import io.chubao.joyqueue.client.internal.metadata.domain.TopicMetadata;
import io.chubao.joyqueue.client.internal.producer.PartitionSelector;
import io.chubao.joyqueue.client.internal.producer.domain.ProduceMessage;

import java.util.List;

/**
 * ProducerHelper
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/26
 */
public class ProducerHelper {

    public static void setPartitions(List<ProduceMessage> messages, short partition) {
        for (ProduceMessage message : messages) {
            message.setPartition(partition);
        }
    }

    public static void clearPartitions(List<ProduceMessage> messages) {
        for (ProduceMessage message : messages) {
            clearPartition(message);
        }
    }

    public static void clearPartition(ProduceMessage message) {
        message.setPartition(ProduceMessage.NONE_PARTITION);
        message.setPartitionKey(ProduceMessage.NONE_PARTITION_KEY);
    }

    public static PartitionMetadata dispatchPartitions(List<ProduceMessage> messages, TopicMetadata topicMetadata, List<PartitionMetadata> partitions, PartitionSelector partitionSelector) {
        return partitionSelector.select(messages.get(0), topicMetadata, partitions);
    }

    public static List<PartitionMetadata> filterBlackList(List<PartitionMetadata> partitions, List<PartitionMetadata> partitionBlackList) {
        List<PartitionMetadata> newPartitions = Lists.newArrayList(partitions);
        for (PartitionMetadata blackPartitionMetadata : partitionBlackList) {
            newPartitions.remove(blackPartitionMetadata);
        }
        return newPartitions;
    }

}