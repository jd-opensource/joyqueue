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
import org.joyqueue.client.internal.cluster.ClusterClientManager;
import org.joyqueue.client.internal.cluster.ClusterManager;
import org.joyqueue.client.internal.consumer.BrokerLoadBalance;
import org.joyqueue.client.internal.consumer.ConsumerIndexManager;
import org.joyqueue.client.internal.consumer.MessageFetcher;
import org.joyqueue.client.internal.consumer.MessagePoller;
import org.joyqueue.client.internal.consumer.callback.PollerListener;
import org.joyqueue.client.internal.consumer.config.ConsumerConfig;
import org.joyqueue.client.internal.consumer.config.FetcherConfig;
import org.joyqueue.client.internal.consumer.coordinator.ConsumerCoordinator;
import org.joyqueue.client.internal.consumer.coordinator.domain.BrokerAssignment;
import org.joyqueue.client.internal.consumer.coordinator.domain.BrokerAssignments;
import org.joyqueue.client.internal.consumer.coordinator.domain.BrokerAssignmentsHolder;
import org.joyqueue.client.internal.consumer.domain.ConsumeMessage;
import org.joyqueue.client.internal.consumer.domain.ConsumeReply;
import org.joyqueue.client.internal.consumer.domain.FetchIndexData;
import org.joyqueue.client.internal.consumer.exception.ConsumerException;
import org.joyqueue.client.internal.consumer.transport.ConsumerClientManager;
import org.joyqueue.client.internal.metadata.domain.PartitionMetadata;
import org.joyqueue.client.internal.metadata.domain.TopicMetadata;
import org.joyqueue.client.internal.nameserver.NameServerConfig;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.toolkit.service.Service;
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * DefaultMessagePoller
 *
 * author: gaohaoxiang
 * date: 2018/12/11
 */
public class DefaultMessagePoller extends Service implements MessagePoller {

    private static final int CUSTOM_BATCH_SIZE = -1;

    protected static final Logger logger = LoggerFactory.getLogger(DefaultMessagePoller.class);

    private ConsumerConfig config;
    private NameServerConfig nameServerConfig;
    private ClusterManager clusterManager;
    private ClusterClientManager clusterClientManager;
    private ConsumerClientManager consumerClientManager;

    private ConsumerCoordinator consumerCoordinator;
    private FetcherConfig fetcherConfig;
    private MessageFetcher messageFetcher;
    private ConsumerIndexManager consumerIndexManager;
    private MessagePollerInner messagePollerInner;
    private ConcurrentMap<String, BrokerAssignmentsHolder> brokerAssignmentCacheMap = Maps.newConcurrentMap();

    public DefaultMessagePoller(ConsumerConfig config, NameServerConfig nameServerConfig, ClusterManager clusterManager,
                                ClusterClientManager clusterClientManager, ConsumerClientManager consumerClientManager) {
        Preconditions.checkArgument(config != null, "consumer can not be null");
        Preconditions.checkArgument(nameServerConfig != null, "nameServer can not be null");
        Preconditions.checkArgument(clusterManager != null, "clusterManager can not be null");
        Preconditions.checkArgument(clusterClientManager != null, "clusterClientManager can not be null");
        Preconditions.checkArgument(consumerClientManager != null, "consumerClientManager can not be null");
        Preconditions.checkArgument(StringUtils.isNotBlank(config.getApp()), "consumer.app not blank");
        Preconditions.checkArgument(config.getPollTimeout() > config.getLongPollTimeout(), "consumer.pollTimeout must be greater than consumer.longPullTimeout");

        this.config = config;
        this.nameServerConfig = nameServerConfig;
        this.clusterManager = clusterManager;
        this.clusterClientManager = clusterClientManager;
        this.consumerClientManager = consumerClientManager;
    }

    @Override
    protected void validate() throws Exception {
        consumerCoordinator = new ConsumerCoordinator(clusterClientManager);
        fetcherConfig = new FetcherConfig();
        messageFetcher = new DefaultMessageFetcher(consumerClientManager, fetcherConfig);
        consumerIndexManager = new DefaultConsumerIndexManager(clusterManager, consumerClientManager);
        messagePollerInner = new MessagePollerInner(config, nameServerConfig, clusterManager, consumerClientManager, messageFetcher);
    }

    @Override
    protected void doStart() throws Exception {
        messageFetcher.start();
        consumerCoordinator.start();
        messagePollerInner.start();
    }

    @Override
    protected void doStop() {
        if (messagePollerInner != null) {
            messagePollerInner.stop();
        }
        if (consumerCoordinator != null) {
            consumerCoordinator.stop();
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
        CompletableFuture<List<ConsumeMessage>> future = new CompletableFuture<>();
        doPoll(topic, CUSTOM_BATCH_SIZE, timeout, timeoutUnit, new CompletableFuturePollerListener(future));
        return future;
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
        List<ConsumeMessage> consumeMessages = doPollPartition(topic, partition, index, 1, config.getPollTimeout(), TimeUnit.MILLISECONDS, null);
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
        return doPollPartition(topic, partition, index, CUSTOM_BATCH_SIZE, config.getPollTimeout(), TimeUnit.MILLISECONDS, null);
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
        return messagePollerInner.fetchPartition(partitionMetadata.getLeader(), topicMetadata, partition, index, batchSize, timeout, timeoutUnit, listener);
    }

    protected List<ConsumeMessage> doPoll(String topic, int batchSize, long timeout, TimeUnit timeoutUnit, PollerListener listener) {
        checkState();
        Preconditions.checkArgument(StringUtils.isNotBlank(topic), "topic not blank");
        Preconditions.checkArgument(timeoutUnit != null, "timeoutUnit not null");

        TopicMetadata topicMetadata = messagePollerInner.getAndCheckTopicMetadata(topic);
        BrokerLoadBalance brokerBalance = messagePollerInner.getBrokerLoadBalance(topic);

        BrokerAssignments brokerAssignments = fetchBrokerAssignment(topicMetadata);
        brokerAssignments = messagePollerInner.filterNotAvailableBrokers(brokerAssignments);

        if (CollectionUtils.isEmpty(brokerAssignments.getAssignments())) {
            logger.warn("no broker available, topic: {}", topicMetadata.getTopic());
            return messagePollerInner.buildPollEmptyResult(listener);
        }

        if (batchSize == CUSTOM_BATCH_SIZE) {
            batchSize = (config.getBatchSize() != ConsumerConfig.NONE_BATCH_SIZE ? config.getBatchSize() : topicMetadata.getConsumerPolicy().getBatchSize());
        }

        BrokerAssignment brokerAssignment = brokerBalance.loadBalance(brokerAssignments);
        return messagePollerInner.fetchTopic(brokerAssignment.getBroker(), topicMetadata, batchSize, timeout, timeoutUnit, listener);
    }

    protected BrokerAssignments fetchBrokerAssignment(TopicMetadata topicMetadata) {
        BrokerAssignmentsHolder brokerAssignmentsHolder = brokerAssignmentCacheMap.get(topicMetadata.getTopic());
        if (brokerAssignmentsHolder != null && !brokerAssignmentsHolder.isExpired(config.getSessionTimeout())) {
            return brokerAssignmentsHolder.getBrokerAssignments();
        }

        BrokerAssignments brokerAssignments = null;
        if (config.isLoadBalance()) {
            brokerAssignments = consumerCoordinator.fetchBrokerAssignment(topicMetadata, config.getAppFullName(), config.getSessionTimeout());
            brokerAssignments = messagePollerInner.filterNotAvailableBrokers(brokerAssignments);
            if (brokerAssignments == null || CollectionUtils.isEmpty(brokerAssignments.getAssignments())) {
                if (config.isFailover()) {
                    logger.debug("no assignment available, assign all broker, topic: {}", topicMetadata.getTopic());
                    brokerAssignments = messagePollerInner.buildAllBrokerAssignments(topicMetadata);
                }
            }
        } else {
            brokerAssignments = messagePollerInner.buildAllBrokerAssignments(topicMetadata);
        }

        brokerAssignments = messagePollerInner.filterRegionBrokers(topicMetadata, brokerAssignments);

        if (topicMetadata.isAllAvailable()) {
            brokerAssignmentCacheMap.put(topicMetadata.getTopic(), new BrokerAssignmentsHolder(brokerAssignments, SystemClock.now()));
        }
        return brokerAssignments;
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
    public FetchIndexData fetchIndex(String topic, short partition) {
        checkState();
        Preconditions.checkArgument(StringUtils.isNotBlank(topic), "topic not blank");

        TopicMetadata topicMetadata = messagePollerInner.getAndCheckTopicMetadata(topic);
        return consumerIndexManager.fetchIndex(topicMetadata.getTopic(), config.getAppFullName(), partition, config.getTimeout());
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
