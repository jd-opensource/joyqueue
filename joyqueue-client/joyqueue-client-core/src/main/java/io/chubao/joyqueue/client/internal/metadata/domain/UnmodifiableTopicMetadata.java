package io.chubao.joyqueue.client.internal.metadata.domain;

import io.chubao.joyqueue.domain.ConsumerPolicy;
import io.chubao.joyqueue.domain.ProducerPolicy;
import io.chubao.joyqueue.domain.TopicType;
import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.network.domain.BrokerNode;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * UnmodifiableTopicMetadata
 *
 * author: gaohaoxiang
 * date: 2019/1/22
 */
@Deprecated
public class UnmodifiableTopicMetadata extends TopicMetadata {

    public UnmodifiableTopicMetadata(String topic, ProducerPolicy producerPolicy, ConsumerPolicy consumerPolicy, TopicType type, List<PartitionGroupMetadata> partitionGroups,
                                     List<PartitionMetadata> partitions, Map<Short, PartitionMetadata> partitionMap, Map<Integer, PartitionGroupMetadata> partitionGroupMap, List<BrokerNode> brokers,
                                     List<BrokerNode> nearbyBrokers, Map<Integer, BrokerNode> brokerMap, Map<Integer, List<PartitionMetadata>> brokerPartitions,
                                     Map<Integer, List<PartitionGroupMetadata>> brokerPartitionGroups, boolean allAvailable, JoyQueueCode code) {
        super(topic, producerPolicy, consumerPolicy, type, partitionGroups, partitions, partitionMap, partitionGroupMap,
                brokers, nearbyBrokers, brokerMap, brokerPartitions, brokerPartitionGroups, allAvailable, code);
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