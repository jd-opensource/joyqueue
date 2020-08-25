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
package org.joyqueue.client.internal.metadata.domain;

import com.google.common.collect.Maps;
import org.joyqueue.domain.ConsumerPolicy;
import org.joyqueue.domain.ProducerPolicy;
import org.joyqueue.domain.TopicType;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.network.domain.BrokerNode;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * TopicMetadata
 *
 * author: gaohaoxiang
 * date: 2018/12/3
 */
public class TopicMetadata implements Serializable {

    private String topic;
    private ProducerPolicy producerPolicy;
    private ConsumerPolicy consumerPolicy;
    private TopicType type;
    private JoyQueueCode code;

    private List<PartitionGroupMetadata> partitionGroups;
    private List<PartitionMetadata> partitions;

    private Map<Short, PartitionMetadata> partitionMap;
    private Map<Integer, PartitionGroupMetadata> partitionGroupMap;

    private List<BrokerNode> brokers;
    private List<BrokerNode> writableBrokers;
    private List<BrokerNode> readableBrokers;
    private List<BrokerNode> nearbyBrokers;
    private List<BrokerNode> writableNearbyBrokers;
    private List<BrokerNode> readableNearbyBrokers;
    private Map<Integer, BrokerNode> brokerMap;
    private Map<Integer, List<PartitionMetadata>> brokerPartitions;
    private Map<Integer, List<PartitionGroupMetadata>> brokerPartitionGroups;

    private boolean allAvailable = false;

    @Deprecated
    private TopicMetadata unmodifiableTopicMetadata;

    private ConcurrentMap<Object, Object> attachments = Maps.newConcurrentMap();

    public TopicMetadata() {

    }

    public TopicMetadata(JoyQueueCode code) {
        this.code = code;
    }

    public TopicMetadata(String topic, ProducerPolicy producerPolicy, ConsumerPolicy consumerPolicy, TopicType type, List<PartitionGroupMetadata> partitionGroups,
                         List<PartitionMetadata> partitions, Map<Short, PartitionMetadata> partitionMap, Map<Integer, PartitionGroupMetadata> partitionGroupMap, List<BrokerNode> brokers,
                         List<BrokerNode> writableBrokers, List<BrokerNode> readableBrokers, List<BrokerNode> nearbyBrokers, List<BrokerNode> writableNearbyBrokers,
                         List<BrokerNode> readableNearbyBrokers, Map<Integer, BrokerNode> brokerMap, Map<Integer, List<PartitionMetadata>> brokerPartitions,
                         Map<Integer, List<PartitionGroupMetadata>> brokerPartitionGroups, boolean allAvailable, JoyQueueCode code) {
        this.topic = topic;
        this.producerPolicy = producerPolicy;
        this.consumerPolicy = consumerPolicy;
        this.type = type;
        this.partitionGroups = partitionGroups;
        this.partitions = partitions;
        this.partitionMap = partitionMap;
        this.partitionGroupMap = partitionGroupMap;
        this.brokers = brokers;
        this.writableBrokers = writableBrokers;
        this.readableBrokers = readableBrokers;
        this.nearbyBrokers = nearbyBrokers;
        this.writableNearbyBrokers = writableNearbyBrokers;
        this.readableNearbyBrokers = readableNearbyBrokers;
        this.brokerMap = brokerMap;
        this.brokerPartitions = brokerPartitions;
        this.brokerPartitionGroups = brokerPartitionGroups;
        this.allAvailable = allAvailable;
        this.code = code;
    }

    public String getTopic() {
        return topic;
    }

    public ProducerPolicy getProducerPolicy() {
        return producerPolicy;
    }

    public ConsumerPolicy getConsumerPolicy() {
        return consumerPolicy;
    }

    public TopicType getType() {
        return type;
    }

    public List<PartitionMetadata> getPartitions() {
        return partitions;
    }

    public List<PartitionGroupMetadata> getPartitionGroups() {
        return partitionGroups;
    }

    public List<BrokerNode> getBrokers() {
        return brokers;
    }

    public List<BrokerNode> getWritableBrokers() {
        return writableBrokers;
    }

    public List<BrokerNode> getReadableBrokers() {
        return readableBrokers;
    }

    public List<BrokerNode> getNearbyBrokers() {
        return nearbyBrokers;
    }

    public List<BrokerNode> getWritableNearbyBrokers() {
        return writableNearbyBrokers;
    }

    public List<BrokerNode> getReadableNearbyBrokers() {
        return readableNearbyBrokers;
    }

    public List<PartitionGroupMetadata> getBrokerPartitionGroups(int brokerId) {
        return brokerPartitionGroups.get(brokerId);
    }

    public List<PartitionMetadata> getBrokerPartitions(int brokerId) {
        return brokerPartitions.get(brokerId);
    }

    public BrokerNode getBroker(int brokerId) {
        return brokerMap.get(brokerId);
    }

    public PartitionGroupMetadata getPartitionGroup(int partitionGroup) {
        return partitionGroupMap.get(partitionGroup);
    }

    public PartitionMetadata getPartition(short partition) {
        return partitionMap.get(partition);
    }

    public boolean isAllAvailable() {
        return allAvailable;
    }

    public JoyQueueCode getCode() {
        return code;
    }

    public void setAttachments(ConcurrentMap<Object, Object> attachments) {
        this.attachments = attachments;
    }

    public ConcurrentMap<Object, Object> getAttachments() {
        return attachments;
    }

    public void putAttachment(Object key, Object value) {
        attachments.put(key, value);
    }

    public <T> T putIfAbsentAttachment(Object key, Object value) {
        return (T) attachments.putIfAbsent(key, value);
    }

    public <T> T getAttachment(Object key) {
        return (T) attachments.get(key);
    }

    @Override
    public String toString() {
        return "TopicMetadata{" +
                "topic='" + topic + '\'' +
                ", producerPolicy=" + producerPolicy +
                ", consumerPolicy=" + consumerPolicy +
                ", type=" + type +
                ", partitionGroups=" + partitionGroups +
                ", partitions=" + partitions +
                ", code=" + code +
                '}';
    }
}