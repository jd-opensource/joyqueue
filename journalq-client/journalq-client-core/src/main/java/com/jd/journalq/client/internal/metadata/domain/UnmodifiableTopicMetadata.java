package com.jd.journalq.client.internal.metadata.domain;

import com.jd.journalq.domain.Consumer;
import com.jd.journalq.domain.Producer;
import com.jd.journalq.domain.Topic;
import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.network.domain.BrokerNode;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * UnmodifiableTopicMetadata
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/1/22
 */
@Deprecated
public class UnmodifiableTopicMetadata extends TopicMetadata {

    public UnmodifiableTopicMetadata(String topic, Producer.ProducerPolicy producerPolicy, Consumer.ConsumerPolicy consumerPolicy, Topic.Type type, List<PartitionGroupMetadata> partitionGroups,
                                     List<PartitionMetadata> partitions, Map<Short, PartitionMetadata> partitionMap, Map<Integer, PartitionGroupMetadata> partitionGroupMap, List<BrokerNode> brokers,
                                     List<BrokerNode> nearbyBrokers, Map<Integer, BrokerNode> brokerMap, Map<Integer, List<PartitionMetadata>> brokerPartitions, Map<Integer, List<PartitionGroupMetadata>> brokerPartitionGroups, JMQCode code) {
        super(topic, producerPolicy, consumerPolicy, type, partitionGroups, partitions, partitionMap, partitionGroupMap, brokers, nearbyBrokers, brokerMap, brokerPartitions, brokerPartitionGroups, code);
    }

    @Override
    public List<PartitionMetadata> getPartitions() {
        return Collections.unmodifiableList(super.getPartitions());
    }

    public List<PartitionGroupMetadata> getPartitionGroups() {
        return Collections.unmodifiableList(super.getPartitionGroups());
    }

    public List<BrokerNode> getBrokers() {
        return Collections.unmodifiableList(super.getBrokers());
    }
}