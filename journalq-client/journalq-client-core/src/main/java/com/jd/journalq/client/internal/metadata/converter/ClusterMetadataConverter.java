package com.jd.journalq.client.internal.metadata.converter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jd.journalq.client.internal.metadata.domain.ClusterMetadata;
import com.jd.journalq.client.internal.metadata.domain.PartitionGroupMetadata;
import com.jd.journalq.client.internal.metadata.domain.PartitionMetadata;
import com.jd.journalq.client.internal.metadata.domain.TopicMetadata;
import com.jd.journalq.common.exception.JMQCode;
import com.jd.journalq.common.network.command.FetchClusterAck;
import com.jd.journalq.common.network.command.Topic;
import com.jd.journalq.common.network.command.TopicPartition;
import com.jd.journalq.common.network.command.TopicPartitionGroup;
import com.jd.journalq.common.network.domain.BrokerNode;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * ClusterMetadataConverter
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/3
 */
public class ClusterMetadataConverter {

    public static ClusterMetadata convert(FetchClusterAck fetchClusterAck) {
        Map<String, TopicMetadata> topics = Maps.newLinkedHashMap();
        Map<Integer, BrokerNode> brokers = fetchClusterAck.getBrokers();

        if (MapUtils.isNotEmpty(fetchClusterAck.getTopics())) {
            for (Map.Entry<String, Topic> topicEntry : fetchClusterAck.getTopics().entrySet()) {
                TopicMetadata topicMetadata = convertTopicMetadata(topicEntry.getKey(), topicEntry.getValue(), fetchClusterAck.getBrokers());
                topics.put(topicEntry.getKey(), topicMetadata);
            }
        }

        return new ClusterMetadata(topics, brokers);
    }

    public static TopicMetadata convertTopicMetadata(String code, Topic topic, Map<Integer, BrokerNode> brokerMap) {
        if (!topic.getCode().equals(JMQCode.SUCCESS)) {
            return new TopicMetadata(topic.getCode());
        }

        List<PartitionGroupMetadata> partitionGroups = Lists.newArrayList();
        List<PartitionMetadata> partitions = Lists.newArrayList();
        Map<Short, PartitionMetadata> partitionMap = Maps.newHashMap();
        Map<Integer, PartitionGroupMetadata> partitionGroupMap = Maps.newHashMap();
        List<BrokerNode> brokers = Lists.newArrayList();
        List<BrokerNode> nearbyBrokers = Lists.newArrayList();
        Map<Integer, List<PartitionMetadata>> brokerPartitions = Maps.newHashMap();
        Map<Integer, List<PartitionGroupMetadata>> brokerPartitionGroups = Maps.newHashMap();

        for (Map.Entry<Integer, BrokerNode> entry : brokerMap.entrySet()) {
            BrokerNode brokerNode = entry.getValue();
            brokers.add(brokerNode);
            if (brokerNode.isNearby()) {
                nearbyBrokers.add(brokerNode);
            }
        }

        for (Map.Entry<Integer, TopicPartitionGroup> entry : topic.getPartitionGroups().entrySet()) {
            PartitionGroupMetadata partitionGroupMetadata = convertPartitionGroupMetadata(code, entry.getValue(), brokerMap);
            partitionGroups.add(partitionGroupMetadata);
            partitionGroupMap.put(entry.getKey(), partitionGroupMetadata);

            if (partitionGroupMetadata.getLeader() != null) {
                List<PartitionGroupMetadata> brokerPartitionGroupList = brokerPartitionGroups.get(partitionGroupMetadata.getLeader().getId());
                if (brokerPartitionGroupList == null) {
                    brokerPartitionGroupList = Lists.newArrayList();
                    brokerPartitionGroups.put(partitionGroupMetadata.getLeader().getId(), brokerPartitionGroupList);
                }
                brokerPartitionGroupList.add(partitionGroupMetadata);
            }

            for (Map.Entry<Short, PartitionMetadata> partitionEntry : partitionGroupMetadata.getPartitions().entrySet()) {
                partitions.add(partitionEntry.getValue());
                partitionMap.put(partitionEntry.getKey(), partitionEntry.getValue());

                if (partitionEntry.getValue().getLeader() != null) {
                    List<PartitionMetadata> brokerPartitionList = brokerPartitions.get(partitionEntry.getValue().getLeader().getId());
                    if (brokerPartitionList == null) {
                        brokerPartitionList = Lists.newArrayList();
                        brokerPartitions.put(partitionEntry.getValue().getLeader().getId(), brokerPartitionList);
                    }
                    brokerPartitionList.add(partitionEntry.getValue());
                }
            }
        }

        if (topic.getProducerPolicy() != null) {
            Map<String, Short> weightMap = topic.getProducerPolicy().getWeight();
            if (MapUtils.isNotEmpty(weightMap)) {
                for (Map.Entry<String, Short> entry : weightMap.entrySet()) {
                    if (!StringUtils.isNumeric(entry.getKey())) {
                        continue;
                    }
                    PartitionGroupMetadata partitionGroupMetadata = partitionGroupMap.get(Integer.valueOf(entry.getKey()));
                    if (partitionGroupMetadata == null || partitionGroupMetadata.getLeader() == null) {
                        continue;
                    }
                    partitionGroupMetadata.getLeader().setWeight(partitionGroupMetadata.getLeader().getWeight() + entry.getValue());
                }
            }
        }

        return new TopicMetadata(code, topic.getProducerPolicy(), topic.getConsumerPolicy(), topic.getType(), partitionGroups, partitions, partitionMap, partitionGroupMap, brokers,
                nearbyBrokers, brokerMap, brokerPartitions, brokerPartitionGroups, topic.getCode());
    }

    public static PartitionGroupMetadata convertPartitionGroupMetadata(String topic, TopicPartitionGroup partitionGroup, Map<Integer, BrokerNode> brokers) {
        Map<Short, PartitionMetadata> partitions = Maps.newLinkedHashMap();
        BrokerNode leader = brokers.get(partitionGroup.getLeader());
        for (Map.Entry<Short, TopicPartition> entry : partitionGroup.getPartitions().entrySet()) {
            PartitionMetadata partitionMetadata = convertPartitionMetadata(topic, entry.getValue(), partitionGroup.getId(), leader);
            partitions.put(entry.getKey(), partitionMetadata);
        }
        return new PartitionGroupMetadata(partitionGroup.getId(), leader, partitions);
    }

    public static PartitionMetadata convertPartitionMetadata(String topic, TopicPartition topicPartition, int partitionGroupId, BrokerNode leader) {
        return new PartitionMetadata(topicPartition.getId(), partitionGroupId, topic, leader);
    }
}