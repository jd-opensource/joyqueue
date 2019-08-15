package io.chubao.joyqueue.client.internal.producer;

import io.chubao.joyqueue.client.internal.metadata.domain.PartitionMetadata;
import io.chubao.joyqueue.client.internal.metadata.domain.TopicMetadata;
import io.chubao.joyqueue.client.internal.producer.domain.ProduceMessage;
import com.jd.laf.extension.Type;

import java.util.List;

/**
 * PartitionSelector
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/10
 */
public interface PartitionSelector extends Type<String> {

    PartitionMetadata select(ProduceMessage message, TopicMetadata topicMetadata, List<PartitionMetadata> partitions);
}