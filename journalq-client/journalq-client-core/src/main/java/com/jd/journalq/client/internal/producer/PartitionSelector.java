package com.jd.journalq.client.internal.producer;

import com.jd.journalq.client.internal.metadata.domain.PartitionMetadata;
import com.jd.journalq.client.internal.metadata.domain.TopicMetadata;
import com.jd.journalq.client.internal.producer.domain.ProduceMessage;
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