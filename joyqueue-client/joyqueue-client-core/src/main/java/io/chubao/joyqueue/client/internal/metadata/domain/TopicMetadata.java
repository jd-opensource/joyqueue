package io.chubao.joyqueue.client.internal.metadata.domain;

import io.chubao.joyqueue.domain.ConsumerPolicy;
import io.chubao.joyqueue.domain.ProducerPolicy;
import io.chubao.joyqueue.domain.TopicType;
import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.network.domain.BrokerNode;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

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
    private List<BrokerNode> nearbyBrokers;
    private Map<Integer, BrokerNode> brokerMap;
    private Map<Integer, List<PartitionMetadata>> brokerPartitions;
    private Map<Integer, List<PartitionGroupMetadata>> brokerPartitionGroups;

    private boolean allAvailable = false;

    @Deprecated
    private TopicMetadata unmodifiableTopicMetadata;

    public TopicMetadata() {

    }

    public TopicMetadata(JoyQueueCode code) {
        this.code = code;
    }

    public TopicMetadata(String topic, ProducerPolicy producerPolicy, ConsumerPolicy consumerPolicy, TopicType type, List<PartitionGroupMetadata> partitionGroups,
                         List<PartitionMetadata> partitions, Map<Short, PartitionMetadata> partitionMap, Map<Integer, PartitionGroupMetadata> partitionGroupMap, List<BrokerNode> brokers,
                         List<BrokerNode> nearbyBrokers, Map<Integer, BrokerNode> brokerMap, Map<Integer, List<PartitionMetadata>> brokerPartitions,
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
        this.nearbyBrokers = nearbyBrokers;
        this.brokerMap = brokerMap;
        this.brokerPartitions = brokerPartitions;
        this.brokerPartitionGroups = brokerPartitionGroups;
        this.allAvailable = allAvailable;
        this.code = code;
    }

    @Deprecated
    public TopicMetadata clone() {
        if (unmodifiableTopicMetadata != null) {
            return unmodifiableTopicMetadata;
        }

        ProducerPolicy newProducerPolicy = null;
        ConsumerPolicy newConsumerPolicy = null;

        if (producerPolicy != null) {
            newProducerPolicy = new ProducerPolicy(producerPolicy.getNearby(), producerPolicy.getSingle(), producerPolicy.getArchive(),
                    producerPolicy.getWeight(), producerPolicy.getBlackList(), producerPolicy.getTimeOut());
        }

        if (consumerPolicy != null) {
            newConsumerPolicy = new ConsumerPolicy(consumerPolicy.getNearby(), consumerPolicy.getPaused(), consumerPolicy.getArchive(), consumerPolicy.getRetry(), consumerPolicy.getSeq(),
                    consumerPolicy.getAckTimeout(), consumerPolicy.getBatchSize(), consumerPolicy.getConcurrent(), consumerPolicy.getDelay(),
                    consumerPolicy.getBlackList(), consumerPolicy.getErrTimes(), consumerPolicy.getMaxPartitionNum(), consumerPolicy.getReadRetryProbability(),null);
        }

        unmodifiableTopicMetadata = new UnmodifiableTopicMetadata(topic, newProducerPolicy, newConsumerPolicy, type, partitionGroups, partitions, partitionMap, partitionGroupMap, brokers,
                nearbyBrokers, brokerMap, brokerPartitions, brokerPartitionGroups, allAvailable, code);
        return unmodifiableTopicMetadata;
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

    public boolean isAllAvailable() {
        return allAvailable;
    }

    public JoyQueueCode getCode() {
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