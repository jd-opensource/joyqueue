/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.client.internal.metadata.converter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.joyqueue.client.internal.metadata.domain.ClusterMetadata;
import org.joyqueue.client.internal.metadata.domain.PartitionGroupMetadata;
import org.joyqueue.client.internal.metadata.domain.PartitionMetadata;
import org.joyqueue.client.internal.metadata.domain.TopicMetadata;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.network.command.FetchClusterResponse;
import org.joyqueue.network.command.Topic;
import org.joyqueue.network.command.TopicPartition;
import org.joyqueue.network.command.TopicPartitionGroup;
import org.joyqueue.network.domain.BrokerNode;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ClusterMetadataConverter
 *
 * author: gaohaoxiang
 * date: 2018/12/3
 */
public class ClusterMetadataConverter {

    public static ClusterMetadata convert(FetchClusterResponse fetchClusterResponse) {
        Map<String, TopicMetadata> topics = Maps.newLinkedHashMap();
        Map<Integer, BrokerNode> brokers = fetchClusterResponse.getBrokers();

        if (MapUtils.isNotEmpty(fetchClusterResponse.getTopics())) {
            for (Map.Entry<String, Topic> topicEntry : fetchClusterResponse.getTopics().entrySet()) {
                TopicMetadata topicMetadata = convertTopicMetadata(topicEntry.getKey(), topicEntry.getValue(), fetchClusterResponse.getBrokers());
                topics.put(topicEntry.getKey(), topicMetadata);
            }
        }

        return new ClusterMetadata(topics, brokers);
    }

    public static TopicMetadata convertTopicMetadata(String code, Topic topic, Map<Integer, BrokerNode> brokerMap) {
        if (!topic.getCode().equals(JoyQueueCode.SUCCESS)) {
            return new TopicMetadata(topic.getCode());
        }

        List<PartitionGroupMetadata> partitionGroups = Lists.newArrayList();
        List<PartitionMetadata> partitions = Lists.newArrayList();
        Map<Short, PartitionMetadata> partitionMap = Maps.newHashMap();
        Map<Integer, PartitionGroupMetadata> partitionGroupMap = Maps.newHashMap();
        List<BrokerNode> brokers = Lists.newArrayList();
        List<BrokerNode> nearbyBrokers = Lists.newArrayList();
        Set<BrokerNode> writableBrokers = Sets.newHashSet();
        Set<BrokerNode> readableBrokers = Sets.newHashSet();
        Set<BrokerNode> writableNearbyBrokers = Sets.newHashSet();
        Set<BrokerNode> readableNearbyBrokers = Sets.newHashSet();
        Map<Integer, List<PartitionMetadata>> brokerPartitions = Maps.newHashMap();
        Map<Integer, List<PartitionGroupMetadata>> brokerPartitionGroups = Maps.newHashMap();
        boolean allAvailable = true;

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

            BrokerNode leader = partitionGroupMetadata.getLeader();

            if (leader == null) {
                allAvailable = false;
            } else {
                List<PartitionGroupMetadata> brokerPartitionGroupList = brokerPartitionGroups.get(leader.getId());
                if (brokerPartitionGroupList == null) {
                    brokerPartitionGroupList = Lists.newArrayList();
                    brokerPartitionGroups.put(leader.getId(), brokerPartitionGroupList);
                }
                brokerPartitionGroupList.add(partitionGroupMetadata);

                if (leader.isWritable()) {
                    writableBrokers.add(leader);
                    if (leader.isNearby()) {
                        writableNearbyBrokers.add(leader);
                    }
                }

                if (leader.isReadable()) {
                    readableBrokers.add(leader);
                    if (leader.isNearby()) {
                        readableNearbyBrokers.add(leader);
                    }
                }
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
                Lists.newArrayList(writableBrokers), Lists.newArrayList(readableBrokers), nearbyBrokers, Lists.newArrayList(writableNearbyBrokers), Lists.newArrayList(readableNearbyBrokers),
                brokerMap, brokerPartitions, brokerPartitionGroups, allAvailable, topic.getCode());
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