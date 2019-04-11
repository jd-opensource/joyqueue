package com.jd.journalq.client.internal.consumer.support;

import com.google.common.collect.Lists;
import com.jd.journalq.client.internal.cluster.ClusterManager;
import com.jd.journalq.client.internal.consumer.BrokerLoadBalance;
import com.jd.journalq.client.internal.consumer.ConsumerIndexManager;
import com.jd.journalq.client.internal.consumer.MessageFetcher;
import com.jd.journalq.client.internal.consumer.MessagePoller;
import com.jd.journalq.client.internal.consumer.callback.ConsumerListener;
import com.jd.journalq.client.internal.consumer.config.ConsumerConfig;
import com.jd.journalq.client.internal.consumer.config.FetcherConfig;
import com.jd.journalq.client.internal.consumer.coordinator.domain.BrokerAssignment;
import com.jd.journalq.client.internal.consumer.coordinator.domain.BrokerAssignments;
import com.jd.journalq.client.internal.consumer.coordinator.domain.PartitionAssignment;
import com.jd.journalq.client.internal.consumer.domain.ConsumeMessage;
import com.jd.journalq.client.internal.consumer.domain.ConsumeReply;
import com.jd.journalq.client.internal.consumer.domain.FetchIndexData;
import com.jd.journalq.client.internal.consumer.exception.ConsumerException;
import com.jd.journalq.client.internal.consumer.transport.ConsumerClientManager;
import com.jd.journalq.client.internal.metadata.domain.PartitionMetadata;
import com.jd.journalq.client.internal.metadata.domain.TopicMetadata;
import com.jd.journalq.client.internal.nameserver.NameServerConfig;
import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.network.domain.BrokerNode;
import com.jd.journalq.toolkit.lang.Preconditions;
import com.jd.journalq.toolkit.service.Service;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * PartitionMessagePoller
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/14
 */
public class PartitionMessagePoller extends Service implements MessagePoller {

    private static final int CUSTOM_BATCH_SIZE = -1;

    protected static final Logger logger = LoggerFactory.getLogger(PartitionMessagePoller.class);

    private ConsumerConfig config;
    private NameServerConfig nameServerConfig;
    private ClusterManager clusterManager;
    private ConsumerClientManager consumerClientManager;

    private FetcherConfig fetcherConfig;
    private MessageFetcher messageFetcher;
    private ConsumerIndexManager consumerIndexManager;
    private MessagePollerInner messagePollerInner;

    public PartitionMessagePoller(ConsumerConfig config, NameServerConfig nameServerConfig, ClusterManager clusterManager, ConsumerClientManager consumerClientManager, ConsumerIndexManager consumerIndexManager) {
        Preconditions.checkArgument(config != null, "consumer not null");
        Preconditions.checkArgument(nameServerConfig != null, "nameServer not null");
        Preconditions.checkArgument(clusterManager != null, "clusterManager not null");
        Preconditions.checkArgument(consumerIndexManager != null, "consumerIndexManager not null");
        Preconditions.checkArgument(consumerClientManager != null, "consumerClientManager not null");
        Preconditions.checkArgument(StringUtils.isNotBlank(config.getApp()), "consumer.app not blank");
        Preconditions.checkArgument(config.getPollTimeout() > config.getLongPollTimeout(), "consumer.pollTimeout must be greater than consumer.longPullTimeout");

        this.config = config;
        this.nameServerConfig = nameServerConfig;
        this.clusterManager = clusterManager;
        this.consumerClientManager = consumerClientManager;
        this.consumerIndexManager = consumerIndexManager;
    }

    @Override
    protected void validate() throws Exception {
        fetcherConfig = new FetcherConfig();
        messageFetcher = new DefaultMessageFetcher(consumerClientManager, fetcherConfig);
        messagePollerInner = new MessagePollerInner(config, nameServerConfig, clusterManager, consumerClientManager, messageFetcher);
    }

    @Override
    protected void doStart() throws Exception {
        messageFetcher.start();
        messagePollerInner.start();
    }

    @Override
    protected void doStop() {
        if (messagePollerInner != null) {
            messagePollerInner.stop();
        }
        if (messageFetcher != null) {
            messageFetcher.stop();
        }
    }

    @Override
    public ConsumeMessage pollOnce(String topic) {
        return pollOnce(topic, config.getPollTimeout(), TimeUnit.MILLISECONDS);
    }

    @Override
    public ConsumeMessage pollOnce(String topic, long timeout, TimeUnit timeoutUnit) {
        List<ConsumeMessage> consumeMessages = doPoll(topic, 1, timeout, timeoutUnit, null);
        if (CollectionUtils.isEmpty(consumeMessages)) {
            return null;
        }
        return consumeMessages.get(0);
    }

    @Override
    public List<ConsumeMessage> poll(String topic) {
        return poll(topic, config.getPollTimeout(), TimeUnit.MILLISECONDS);
    }

    @Override
    public List<ConsumeMessage> poll(String topic, long timeout, TimeUnit timeoutUnit) {
        return doPoll(topic, CUSTOM_BATCH_SIZE, timeout, timeoutUnit, null);
    }

    @Override
    public void pollAsync(String topic, ConsumerListener listener) {
        Preconditions.checkArgument(listener != null, "listener not null");
        pollAsync(topic, config.getPollTimeout(), TimeUnit.MILLISECONDS, listener);
    }

    @Override
    public void pollAsync(String topic, long timeout, TimeUnit timeoutUnit, ConsumerListener listener) {
        Preconditions.checkArgument(listener != null, "listener not null");
        pollAsync(topic, timeout, timeoutUnit, listener);
    }

    @Override
    public ConsumeMessage pollPartitionOnce(String topic, short partition) {
        return pollPartitionOnce(topic, partition, config.getPollTimeout(), TimeUnit.MILLISECONDS);
    }

    @Override
    public ConsumeMessage pollPartitionOnce(String topic, short partition, long timeout, TimeUnit timeoutUnit) {
        List<ConsumeMessage> consumeMessages = doPollPartition(topic, partition, 1, timeout, timeoutUnit, null);
        if (CollectionUtils.isEmpty(consumeMessages)) {
            return null;
        }
        return consumeMessages.get(0);
    }

    @Override
    public ConsumeMessage pollPartitionOnce(String topic, short partition, long index) {
        return pollPartitionOnce(topic, partition, index, config.getPollTimeout(), TimeUnit.MILLISECONDS);
    }

    @Override
    public ConsumeMessage pollPartitionOnce(String topic, short partition, long index, long timeout, TimeUnit timeoutUnit) {
        List<ConsumeMessage> consumeMessages = doPollPartition(topic, partition, index, 1, timeout, timeoutUnit, null);
        if (CollectionUtils.isEmpty(consumeMessages)) {
            return null;
        }
        return consumeMessages.get(0);
    }

    @Override
    public List<ConsumeMessage> pollPartition(String topic, short partition) {
        return pollPartition(topic, partition, config.getPollTimeout(), TimeUnit.MILLISECONDS);
    }

    @Override
    public List<ConsumeMessage> pollPartition(String topic, short partition, long timeout, TimeUnit timeoutUnit) {
        return doPollPartition(topic, partition, CUSTOM_BATCH_SIZE, timeout, timeoutUnit, null);
    }

    @Override
    public List<ConsumeMessage> pollPartition(String topic, short partition, long index) {
        return pollPartition(topic, partition, index, config.getPollTimeout(), TimeUnit.MILLISECONDS);
    }

    @Override
    public List<ConsumeMessage> pollPartition(String topic, short partition, long index, long timeout, TimeUnit timeoutUnit) {
        return doPollPartition(topic, partition, index, CUSTOM_BATCH_SIZE, timeout, timeoutUnit, null);
    }

    @Override
    public void pollPartitionAsync(String topic, short partition, ConsumerListener listener) {
        pollPartitionAsync(topic, partition, config.getPollTimeout(), TimeUnit.MILLISECONDS, listener);
    }

    @Override
    public void pollPartitionAsync(String topic, short partition, long timeout, TimeUnit timeoutUnit, ConsumerListener listener) {
        Preconditions.checkArgument(listener != null, "listener not null");
        doPollPartition(topic, partition, CUSTOM_BATCH_SIZE, timeout, timeoutUnit, listener);
    }

    @Override
    public void pollPartitionAsync(String topic, short partition, long index, ConsumerListener listener) {
        pollPartitionAsync(topic, partition, index, config.getPollTimeout(), TimeUnit.MILLISECONDS, listener);
    }

    @Override
    public void pollPartitionAsync(String topic, short partition, long index, long timeout, TimeUnit timeoutUnit, ConsumerListener listener) {
        Preconditions.checkArgument(listener != null, "listener not null");
        doPollPartition(topic, partition, index, CUSTOM_BATCH_SIZE, timeout, timeoutUnit, listener);
    }

    protected List<ConsumeMessage> doPollPartition(String topic, short partition, int batchSize, long timeout, TimeUnit timeoutUnit, ConsumerListener listener) {
        return doPollPartition(topic, partition, MessagePollerInner.FETCH_PARTITION_NONE_INDEX, batchSize, timeout, timeoutUnit, listener);
    }

    protected List<ConsumeMessage> doPollPartition(String topic, short partition, long index, int batchSize, long timeout, TimeUnit timeoutUnit, ConsumerListener listener) {
        checkState();
        Preconditions.checkArgument(StringUtils.isNotBlank(topic), "topic not blank");
        Preconditions.checkArgument(timeoutUnit != null, "timeoutUnit not null");

        TopicMetadata topicMetadata = messagePollerInner.getAndCheckTopicMetadata(topic);
        PartitionMetadata partitionMetadata = topicMetadata.getPartition(partition);

        if (partitionMetadata == null) {
            throw new ConsumerException(String.format("partition not exist, topic: %s, partition: %s", topic, partition), JMQCode.FW_TOPIC_NO_PARTITIONGROUP.getCode());
        }

        if (partitionMetadata.getLeader() == null) {
            throw new ConsumerException(String.format("partition not available, topic: %s, partition: %s", topic, partition), JMQCode.FW_TOPIC_NO_PARTITIONGROUP.getCode());
        }

        if (batchSize == CUSTOM_BATCH_SIZE) {
            batchSize = (config.getBatchSize() == ConsumerConfig.NONE_BATCH_SIZE ? topicMetadata.getConsumerPolicy().getBatchSize() : config.getBatchSize());
        }

        if (index == MessagePollerInner.FETCH_PARTITION_NONE_INDEX) {
            return doPollPartitionInternal(partitionMetadata.getLeader(), topic, partition, batchSize, timeout, timeoutUnit, listener);
        } else {
            return doPollPartitionInternal(partitionMetadata.getLeader(), topic, partition, index, batchSize, timeout, timeoutUnit, listener);
        }
    }

    protected List<ConsumeMessage> doPoll(String topic, int batchSize, long timeout, TimeUnit timeoutUnit, ConsumerListener listener) {
        checkState();
        Preconditions.checkArgument(StringUtils.isNotBlank(topic), "topic not blank");
        Preconditions.checkArgument(timeoutUnit != null, "timeoutUnit not null");

        TopicMetadata topicMetadata = messagePollerInner.getAndCheckTopicMetadata(topic);
        BrokerAssignments brokerAssignments = buildAllBrokerAssignments(topicMetadata);
        brokerAssignments = messagePollerInner.filterRegionBrokers(topicMetadata, brokerAssignments);
        brokerAssignments = messagePollerInner.filterNotAvailableBrokers(brokerAssignments);

        if (CollectionUtils.isEmpty(brokerAssignments.getAssignments())) {
            logger.warn("no broker available, topic: {}", topicMetadata.getTopic());
            return messagePollerInner.buildPollEmptyResult(listener);
        }

        BrokerLoadBalance brokerBalance = messagePollerInner.getBrokerLoadBalance(topicMetadata.getTopic());
        BrokerAssignment brokerAssignment = brokerBalance.loadBalance(brokerAssignments);
        short partition = brokerAssignment.getPartitionAssignment().getPartitions().get(0);

        if (batchSize == CUSTOM_BATCH_SIZE) {
            batchSize = (config.getBatchSize() != ConsumerConfig.NONE_BATCH_SIZE ? config.getBatchSize() : topicMetadata.getConsumerPolicy().getBatchSize());
        }

        return doPollPartitionInternal(brokerAssignment.getBroker(), topic, partition, batchSize, timeout, timeoutUnit, listener);
    }

    protected List<ConsumeMessage> doPollPartitionInternal(BrokerNode brokerNode, String topic, short partition, int batchSize, long timeout, TimeUnit timeoutUnit, ConsumerListener listener) {
        FetchIndexData indexData = consumerIndexManager.fetchIndex(topic, messagePollerInner.getAppFullName(), partition, config.getTimeout());
        if (!indexData.getCode().equals(JMQCode.SUCCESS)) {
            logger.error("fetch index error, topic: {}, partition: {}, app: {}, error:{}", topic, partition, messagePollerInner.getAppFullName(), indexData.getCode().getMessage());
            return messagePollerInner.buildPollEmptyResult(listener);
        }

        return doPollPartitionInternal(brokerNode, topic, partition, indexData.getIndex(), batchSize, timeout, timeoutUnit, listener);
    }

    protected List<ConsumeMessage> doPollPartitionInternal(BrokerNode brokerNode, String topic, short partition, long index, int batchSize, long timeout, TimeUnit timeoutUnit, ConsumerListener listener) {
        try {
            return messagePollerInner.fetchPartition(brokerNode, topic, partition, index, batchSize, timeout, timeoutUnit, listener);
        } catch (ConsumerException e) {
            if (e.getCode() == JMQCode.FW_FETCH_MESSAGE_INDEX_OUT_OF_RANGE.getCode()) {
                consumerIndexManager.resetIndex(topic, config.getApp(), partition, config.getTimeout());
                return messagePollerInner.buildPollEmptyResult(listener);
            }
            throw e;
        }
    }

    protected BrokerAssignments buildAllBrokerAssignments(TopicMetadata topicMetadata) {
        List<BrokerAssignment> assignments = Lists.newLinkedList();
        for (PartitionMetadata partitionMetadata : topicMetadata.getPartitions()) {
            if (partitionMetadata.getLeader() == null) {
                continue;
            }
            assignments.add(new BrokerAssignment(partitionMetadata.getLeader(), new PartitionAssignment(Lists.newArrayList(partitionMetadata.getId()))));
        }
        return new BrokerAssignments(assignments);
    }

    @Override
    public synchronized JMQCode reply(String topic, List<ConsumeReply> replyList) {
        checkState();
        Preconditions.checkArgument(StringUtils.isNotBlank(topic), "topic not blank");

        TopicMetadata topicMetadata = messagePollerInner.getAndCheckTopicMetadata(topic);

        if (CollectionUtils.isEmpty(replyList)) {
            throw new IllegalArgumentException(String.format("topic %s reply is empty", topic));
        }

        JMQCode result = consumerIndexManager.commitReply(topicMetadata.getTopic(), replyList, messagePollerInner.getAppFullName(), config.getTimeout());
        if (!result.equals(JMQCode.SUCCESS)) {
            // TODO 临时日志
            logger.warn("commit ack error, topic : {}, code: {}, error: {}", topic, result.getCode(), result.getMessage());
        }
        return result;
    }

    @Override
    public JMQCode replyOnce(String topic, ConsumeReply reply) {
        return reply(topic, Lists.newArrayList(reply));
    }

    @Override
    public TopicMetadata getTopicMetadata(String topic) {
        checkState();
        Preconditions.checkArgument(StringUtils.isNotBlank(topic), "topic not blank");

        String topicFullName = messagePollerInner.getTopicFullName(topic);
        return clusterManager.fetchTopicMetadata(topicFullName, messagePollerInner.getAppFullName());
    }

    protected void checkState() {
        if (!isStarted()) {
            throw new ConsumerException("consumer is not started", JMQCode.CN_SERVICE_NOT_AVAILABLE.getCode());
        }
    }
}