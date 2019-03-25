package io.openmessaging.journalq.extension;

import com.google.common.collect.Lists;
import com.jd.journalq.client.internal.metadata.domain.PartitionMetadata;
import com.jd.journalq.client.internal.metadata.domain.TopicMetadata;
import io.openmessaging.extension.QueueMetaData;

import java.util.List;

/**
 * QueueMetaDataAdapter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/3/1
 */
public class QueueMetaDataAdapter implements QueueMetaData {

    private TopicMetadata topicMetadata;

    public QueueMetaDataAdapter(TopicMetadata topicMetadata) {
        this.topicMetadata = topicMetadata;
    }

    @Override
    public String queueName() {
        return topicMetadata.getTopic();
    }

    @Override
    public List<Partition> partitions() {
        List<PartitionMetadata> partitions = topicMetadata.getPartitions();
        List<Partition> result = Lists.newArrayListWithCapacity(partitions.size());
        for (PartitionMetadata partition : partitions) {
            result.add(new PartitionAdapter(partition));
        }
        return result;
    }

    @Override
    public int hashCode() {
        return topicMetadata.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof QueueMetaDataAdapter)) {
            return false;
        }
        return topicMetadata.equals(((QueueMetaDataAdapter) obj).getTopicMetadata());
    }

    @Override
    public String toString() {
        return topicMetadata.toString();
    }

    public TopicMetadata getTopicMetadata() {
        return topicMetadata;
    }
}