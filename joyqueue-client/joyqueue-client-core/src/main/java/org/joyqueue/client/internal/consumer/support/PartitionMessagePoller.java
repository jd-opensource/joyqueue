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
package org.joyqueue.client.internal.consumer.support;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joyqueue.client.internal.cluster.ClusterManager;
import org.joyqueue.client.internal.consumer.BrokerLoadBalance;
import org.joyqueue.client.internal.consumer.ConsumerIndexManager;
import org.joyqueue.client.internal.consumer.MessageFetcher;
import org.joyqueue.client.internal.consumer.MessagePoller;
import org.joyqueue.client.internal.consumer.callback.PollerListener;
import org.joyqueue.client.internal.consumer.config.ConsumerConfig;
import org.joyqueue.client.internal.consumer.config.FetcherConfig;
import org.joyqueue.client.internal.consumer.coordinator.domain.BrokerAssignment;
import org.joyqueue.client.internal.consumer.coordinator.domain.BrokerAssignments;
import org.joyqueue.client.internal.consumer.coordinator.domain.PartitionAssignment;
import org.joyqueue.client.internal.consumer.domain.ConsumeMessage;
import org.joyqueue.client.internal.consumer.domain.ConsumeReply;
import org.joyqueue.client.internal.consumer.domain.FetchIndexData;
import org.joyqueue.client.internal.consumer.exception.ConsumerException;
import org.joyqueue.client.internal.consumer.transport.ConsumerClientManager;
import org.joyqueue.client.internal.metadata.domain.PartitionMetadata;
import org.joyqueue.client.internal.metadata.domain.TopicMetadata;
import org.joyqueue.client.internal.nameserver.NameServerConfig;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.network.domain.BrokerNode;
import org.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * PartitionMessagePoller
 *
 * author: gaohaoxiang
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

    public PartitionMessagePoller(ConsumerConfig config, NameServerConfig nameServerConfig, ClusterManager clusterManager,
                                  ConsumerClientManager consumerClientManager, ConsumerIndexManager consumerIndexManager) {
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
    public CompletableFuture<List<ConsumeMessage>> pollAsync(String topic) {
        return pollAsync(topic, config.getPollTimeout(), TimeUnit.MILLISECONDS);
    }

    @Override
    public CompletableFuture<List<ConsumeMessage>> pollAsync(String topic, long timeout, TimeUnit timeoutUnit) {
        return pollAsync(topic, timeout, timeoutUnit);
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
    public CompletableFuture<List<ConsumeMessage>> pollPartitionAsync(String topic, short partition) {
        return pollPartitionAsync(topic, partition, config.getPollTimeout(), TimeUnit.MILLISECONDS);
    }

    @Override
    public CompletableFuture<List<ConsumeMessage>> pollPartitionAsync(String topic, short partition, long timeout, TimeUnit timeoutUnit) {
        CompletableFuture<List<ConsumeMessage>> future = new CompletableFuture<>();
        doPollPartition(topic, partition, CUSTOM_BATCH_SIZE, timeout, timeoutUnit, new CompletableFuturePollerListener(future));
        return future;
    }

    @Override
    public CompletableFuture<List<ConsumeMessage>> pollPartitionAsync(String topic, short partition, long index) {
        return pollPartitionAsync(topic, partition, index, config.getPollTimeout(), TimeUnit.MILLISECONDS);
    }

    @Override
    public CompletableFuture<List<ConsumeMessage>> pollPartitionAsync(String topic, short partition, long index, long timeout, TimeUnit timeoutUnit) {
        CompletableFuture<List<ConsumeMessage>> future = new CompletableFuture<>();
        doPollPartition(topic, partition, index, CUSTOM_BATCH_SIZE, timeout, timeoutUnit, new CompletableFuturePollerListener(future));
        return future;
    }

    protected List<ConsumeMessage> doPollPartition(String topic, short partition, int batchSize, long timeout, TimeUnit timeoutUnit, PollerListener listener) {
        return doPollPartition(topic, partition, MessagePollerInner.FETCH_PARTITION_NONE_INDEX, batchSize, timeout, timeoutUnit, listener);
    }

    protected List<ConsumeMessage> doPollPartition(String topic, short partition, long index, int batchSize, long timeout, TimeUnit timeoutUnit, PollerListener listener) {
        checkState();
        Preconditions.checkArgument(StringUtils.isNotBlank(topic), "topic not blank");
        Preconditions.checkArgument(timeoutUnit != null, "timeoutUnit not null");

        TopicMetadata topicMetadata = messagePollerInner.getAndCheckTopicMetadata(topic);
        PartitionMetadata partitionMetadata = topicMetadata.getPartition(partition);

        if (partitionMetadata == null) {
            throw new ConsumerException(String.format("partition not exist, topic: %s, partition: %s", topic, partition), JoyQueueCode.FW_TOPIC_NO_PARTITIONGROUP.getCode());
        }

        if (partitionMetadata.getLeader() == null) {
            throw new ConsumerException(String.format("partition not available, topic: %s, partition: %s", topic, partition), JoyQueueCode.FW_TOPIC_NO_PARTITIONGROUP.getCode());
        }
        if (!partitionMetadata.getLeader().isReadable()) {
            throw new ConsumerException(String.format("partition not readable, topic: %s, partition: %s", topic, partition), JoyQueueCode.FW_TOPIC_NO_PARTITIONGROUP.getCode());
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

    protected List<ConsumeMessage> doPoll(String topic, int batchSize, long timeout, TimeUnit timeoutUnit, PollerListener listener) {
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

    protected List<ConsumeMessage> doPollPartitionInternal(BrokerNode brokerNode, String topic, short partition, int batchSize, long timeout, TimeUnit timeoutUnit, PollerListener listener) {
        FetchIndexData indexData = consumerIndexManager.fetchIndex(topic, config.getAppFullName(), partition, config.getTimeout());
        if (!indexData.getCode().equals(JoyQueueCode.SUCCESS)) {
            logger.error("fetch index error, topic: {}, partition: {}, app: {}, error:{}", topic, partition, config.getAppFullName(), indexData.getCode().getMessage());
            return messagePollerInner.buildPollEmptyResult(listener);
        }

        return doPollPartitionInternal(brokerNode, topic, partition, indexData.getIndex(), batchSize, timeout, timeoutUnit, listener);
    }

    protected List<ConsumeMessage> doPollPartitionInternal(BrokerNode brokerNode, String topic, short partition, long index,
                                                           int batchSize, long timeout, TimeUnit timeoutUnit, PollerListener listener) {
        try {
            return messagePollerInner.fetchPartition(brokerNode, topic, partition, index, batchSize, timeout, timeoutUnit, listener);
        } catch (ConsumerException e) {
            if (e.getCode() == JoyQueueCode.FW_FETCH_MESSAGE_INDEX_OUT_OF_RANGE.getCode()) {
                consumerIndexManager.resetIndex(topic, config.getApp(), partition, config.getTimeout());
                return messagePollerInner.buildPollEmptyResult(listener);
            } else {
                throw e;
            }
        }
    }

    protected BrokerAssignments buildAllBrokerAssignments(TopicMetadata topicMetadata) {
        List<BrokerAssignment> assignments = Lists.newLinkedList();
        for (PartitionMetadata partitionMetadata : topicMetadata.getPartitions()) {
            if (partitionMetadata.getLeader() == null || !partitionMetadata.getLeader().isReadable()) {
                continue;
            }
            assignments.add(new BrokerAssignment(partitionMetadata.getLeader(), new PartitionAssignment(Lists.newArrayList(partitionMetadata.getId()))));
        }
        return new BrokerAssignments(assignments);
    }

    @Override
    public synchronized JoyQueueCode reply(String topic, List<ConsumeReply> replyList) {
        checkState();
        Preconditions.checkArgument(StringUtils.isNotBlank(topic), "topic not blank");

        TopicMetadata topicMetadata = messagePollerInner.getAndCheckTopicMetadata(topic);

        if (CollectionUtils.isEmpty(replyList)) {
            throw new IllegalArgumentException(String.format("topic %s reply is empty", topic));
        }

        JoyQueueCode result = consumerIndexManager.commitReply(topicMetadata.getTopic(), replyList, config.getAppFullName(), config.getTimeout());
        if (!result.equals(JoyQueueCode.SUCCESS)) {
            logger.error("commit ack error, topic : {}, code: {}, error: {}", topic, result.getCode(), result.getMessage());
        }
        return result;
    }

    @Override
    public JoyQueueCode replyOnce(String topic, ConsumeReply reply) {
        return reply(topic, Lists.newArrayList(reply));
    }

    @Override
    public JoyQueueCode commitIndex(String topic, short partition, long index) {
        checkState();
        Preconditions.checkArgument(StringUtils.isNotBlank(topic), "topic not blank");

        TopicMetadata topicMetadata = messagePollerInner.getAndCheckTopicMetadata(topic);

        PartitionMetadata partitionMetadata = topicMetadata.getPartition(partition);
        Preconditions.checkArgument(partitionMetadata != null, "partition does not exist");

        return consumerIndexManager.commitIndex(topic, config.getAppFullName(), partition, index, config.getTimeout());
    }

    @Override
    public JoyQueueCode commitMaxIndex(String topic, short partition) {
        return commitIndex(topic, partition, ConsumerIndexManager.MAX_INDEX);
    }

    @Override
    public JoyQueueCode commitMaxIndex(String topic) {
        checkState();
        Preconditions.checkArgument(StringUtils.isNotBlank(topic), "topic not blank");

        TopicMetadata topicMetadata = messagePollerInner.getAndCheckTopicMetadata(topic);
        Map<Short, Long> partitionMap = Maps.newHashMap();

        for (PartitionMetadata partition : topicMetadata.getPartitions()) {
            partitionMap.put(partition.getId(), ConsumerIndexManager.MAX_INDEX);
        }

        Map<Short, JoyQueueCode> result = consumerIndexManager.batchCommitIndex(topic, config.getAppFullName(), partitionMap, config.getTimeout());

        for (Map.Entry<Short, JoyQueueCode> entry : result.entrySet()) {
            if (!entry.getValue().equals(JoyQueueCode.SUCCESS)) {
                return entry.getValue();
            }
        }
        return JoyQueueCode.SUCCESS;
    }

    @Override
    public JoyQueueCode commitMinIndex(String topic, short partition) {
        return commitIndex(topic, partition, ConsumerIndexManager.MIN_INDEX);
    }

    @Override
    public JoyQueueCode commitMinIndex(String topic) {
        checkState();
        Preconditions.checkArgument(StringUtils.isNotBlank(topic), "topic not blank");

        TopicMetadata topicMetadata = messagePollerInner.getAndCheckTopicMetadata(topic);
        Map<Short, Long> partitionMap = Maps.newHashMap();

        for (PartitionMetadata partition : topicMetadata.getPartitions()) {
            partitionMap.put(partition.getId(), ConsumerIndexManager.MIN_INDEX);
        }

        Map<Short, JoyQueueCode> result = consumerIndexManager.batchCommitIndex(topic, config.getAppFullName(), partitionMap, config.getTimeout());

        for (Map.Entry<Short, JoyQueueCode> entry : result.entrySet()) {
            if (!entry.getValue().equals(JoyQueueCode.SUCCESS)) {
                return entry.getValue();
            }
        }
        return JoyQueueCode.SUCCESS;
    }

    @Override
    public FetchIndexData fetchIndex(String topic, short partition) {
        checkState();
        Preconditions.checkArgument(StringUtils.isNotBlank(topic), "topic not blank");

        TopicMetadata topicMetadata = messagePollerInner.getAndCheckTopicMetadata(topic);
        return consumerIndexManager.fetchIndex(topicMetadata.getTopic(), config.getAppFullName(), partition, config.getTimeout());
    }

    @Override
    public TopicMetadata getTopicMetadata(String topic) {
        checkState();
        Preconditions.checkArgument(StringUtils.isNotBlank(topic), "topic not blank");

        String topicFullName = messagePollerInner.getTopicFullName(topic);
        return clusterManager.fetchTopicMetadata(topicFullName, config.getAppFullName());
    }

    protected void checkState() {
        if (!isStarted()) {
            throw new ConsumerException("consumer is not started", JoyQueueCode.CN_SERVICE_NOT_AVAILABLE.getCode());
        }
    }
}
