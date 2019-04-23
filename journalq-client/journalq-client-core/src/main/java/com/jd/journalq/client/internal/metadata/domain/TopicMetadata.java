/**
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
package com.jd.journalq.client.internal.metadata.domain;

import com.jd.journalq.domain.Consumer;
import com.jd.journalq.domain.Producer;
import com.jd.journalq.domain.Topic;
import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.network.domain.BrokerNode;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * TopicMetadata
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/3
 */
public class TopicMetadata implements Serializable {

    private String topic;
    private Producer.ProducerPolicy producerPolicy;
    private Consumer.ConsumerPolicy consumerPolicy;
    private Topic.Type type;
    private JMQCode code;

    private List<PartitionGroupMetadata> partitionGroups;
    private List<PartitionMetadata> partitions;

    private Map<Short, PartitionMetadata> partitionMap;
    private Map<Integer, PartitionGroupMetadata> partitionGroupMap;

    private List<BrokerNode> brokers;
    private List<BrokerNode> nearbyBrokers;
    private Map<Integer, BrokerNode> brokerMap;
    private Map<Integer, List<PartitionMetadata>> brokerPartitions;
    private Map<Integer, List<PartitionGroupMetadata>> brokerPartitionGroups;

    @Deprecated
    private TopicMetadata unmodifiableTopicMetadata;

    public TopicMetadata() {

    }

    public TopicMetadata(JMQCode code) {
        this.code = code;
    }

    public TopicMetadata(String topic, Producer.ProducerPolicy producerPolicy, Consumer.ConsumerPolicy consumerPolicy, Topic.Type type, List<PartitionGroupMetadata> partitionGroups,
                         List<PartitionMetadata> partitions, Map<Short, PartitionMetadata> partitionMap, Map<Integer, PartitionGroupMetadata> partitionGroupMap, List<BrokerNode> brokers,
                         List<BrokerNode> nearbyBrokers, Map<Integer, BrokerNode> brokerMap, Map<Integer, List<PartitionMetadata>> brokerPartitions,
                         Map<Integer, List<PartitionGroupMetadata>> brokerPartitionGroups, JMQCode code) {
        this.topic = topic;
        this.producerPolicy = producerPolicy;
        this.consumerPolicy = consumerPolicy;
        this.type = type;
        this.partitionGroups = partitionGroups;
        this.partitions = partitions;
        this.partitionMap = partitionMap;
        this.partitionGroupMap = partitionGroupMap;
        this.brokers = brokers;
        this.nearbyBrokers = nearbyBrokers;
        this.brokerMap = brokerMap;
        this.brokerPartitions = brokerPartitions;
        this.brokerPartitionGroups = brokerPartitionGroups;
        this.code = code;
    }

    @Deprecated
    public TopicMetadata clone() {
        if (unmodifiableTopicMetadata != null) {
            return unmodifiableTopicMetadata;
        }

        Producer.ProducerPolicy newProducerPolicy = null;
        Consumer.ConsumerPolicy newConsumerPolicy = null;

        if (producerPolicy != null) {
            newProducerPolicy = new Producer.ProducerPolicy(producerPolicy.getNearby(), producerPolicy.isSingle(), producerPolicy.getArchive(),
                    producerPolicy.getWeight(), producerPolicy.getBlackList(), producerPolicy.getTimeOut());
        }

        if (consumerPolicy != null) {
            newConsumerPolicy = new Consumer.ConsumerPolicy(consumerPolicy.getNearby(), consumerPolicy.getPaused(), consumerPolicy.getArchive(), consumerPolicy.getRetry(), consumerPolicy.getSeq(),
                    consumerPolicy.getAckTimeout(), consumerPolicy.getBatchSize(), consumerPolicy.getConcurrent(), consumerPolicy.getDelay(),
                    consumerPolicy.getBlackList(), consumerPolicy.getErrTimes(), consumerPolicy.getMaxPartitionNum(), consumerPolicy.getReadRetryProbability(),null);
        }

        unmodifiableTopicMetadata = new UnmodifiableTopicMetadata(topic, newProducerPolicy, newConsumerPolicy, type, partitionGroups, partitions, partitionMap, partitionGroupMap, brokers,
                nearbyBrokers, brokerMap, brokerPartitions, brokerPartitionGroups, code);
        return unmodifiableTopicMetadata;
    }

    public String getTopic() {
        return topic;
    }

    public Producer.ProducerPolicy getProducerPolicy() {
        return producerPolicy;
    }

    public Consumer.ConsumerPolicy getConsumerPolicy() {
        return consumerPolicy;
    }

    public Topic.Type getType() {
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

    public List<BrokerNode> getNearbyBrokers() {
        return nearbyBrokers;
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

    public JMQCode getCode() {
        return code;
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