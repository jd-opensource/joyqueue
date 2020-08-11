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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joyqueue.client.internal.cluster.ClusterManager;
import org.joyqueue.client.internal.consumer.BrokerLoadBalance;
import org.joyqueue.client.internal.consumer.MessageFetcher;
import org.joyqueue.client.internal.consumer.callback.FetchListener;
import org.joyqueue.client.internal.consumer.callback.PartitionFetchListener;
import org.joyqueue.client.internal.consumer.callback.PollerListener;
import org.joyqueue.client.internal.consumer.config.ConsumerConfig;
import org.joyqueue.client.internal.consumer.converter.BrokerAssignmentConverter;
import org.joyqueue.client.internal.consumer.coordinator.domain.BrokerAssignment;
import org.joyqueue.client.internal.consumer.coordinator.domain.BrokerAssignments;
import org.joyqueue.client.internal.consumer.domain.ConsumeMessage;
import org.joyqueue.client.internal.consumer.domain.FetchMessageData;
import org.joyqueue.client.internal.consumer.exception.ConsumerException;
import org.joyqueue.client.internal.consumer.transport.ConsumerClient;
import org.joyqueue.client.internal.consumer.transport.ConsumerClientManager;
import org.joyqueue.client.internal.metadata.domain.TopicMetadata;
import org.joyqueue.client.internal.nameserver.NameServerConfig;
import org.joyqueue.client.internal.nameserver.helper.NameServerHelper;
import org.joyqueue.client.internal.trace.TraceBuilder;
import org.joyqueue.client.internal.trace.TraceCaller;
import org.joyqueue.client.internal.trace.TraceType;
import org.joyqueue.client.internal.transport.ClientState;
import org.joyqueue.domain.ConsumerPolicy;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.network.domain.BrokerNode;
import org.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * MessagePollerInner
 *
 * author: gaohaoxiang
 * date: 2019/1/4
 */
public class MessagePollerInner extends Service {

    public static final long FETCH_PARTITION_NONE_INDEX = -1;

    protected static final Logger logger = LoggerFactory.getLogger(MessagePollerInner.class);

    private ConsumerConfig config;
    private NameServerConfig nameServerConfig;
    private ClusterManager clusterManager;
    private ConsumerClientManager consumerClientManager;
    private MessageFetcher messageFetcher;
    private BrokerLoadBalanceManager brokerLoadBalanceManager;

    private String appFullName;

    public MessagePollerInner(ConsumerConfig config, NameServerConfig nameServerConfig, ClusterManager clusterManager, ConsumerClientManager consumerClientManager, MessageFetcher messageFetcher) {
        this.config = config;
        this.nameServerConfig = nameServerConfig;
        this.clusterManager = clusterManager;
        this.consumerClientManager = consumerClientManager;
        this.messageFetcher = messageFetcher;
    }

    @Override
    protected void validate() throws Exception {
        brokerLoadBalanceManager = new BrokerLoadBalanceManager();
    }

    public List<ConsumeMessage> fetchTopic(BrokerNode brokerNode, String topic, int batchSize, long timeout, TimeUnit timeoutUnit, PollerListener listener) {
        Preconditions.checkArgument(StringUtils.isNotBlank(topic), "topic not blank");

        TopicMetadata topicMetadata = getAndCheckTopicMetadata(topic);
        return fetchTopic(brokerNode, topicMetadata, batchSize, timeout, timeoutUnit, listener);
    }

    public List<ConsumeMessage> fetchTopic(BrokerNode brokerNode, TopicMetadata topicMetadata, int batchSize, long timeout, TimeUnit timeoutUnit, PollerListener listener) {
        Preconditions.checkArgument(timeoutUnit != null, "timeoutUnit not null");

        TraceCaller caller = buildTraceCaller(topicMetadata);
        try {
            List<ConsumeMessage> consumeMessages = doFetchTopic(brokerNode, topicMetadata, batchSize, timeout, timeoutUnit, listener);
            caller.end();
            return consumeMessages;
        } catch (Exception e) {
            caller.error();
            if (e instanceof ConsumerException) {
                throw (ConsumerException) e;
            } else {
                throw new ConsumerException(e);
            }
        }
    }

    protected List<ConsumeMessage> doFetchTopic(BrokerNode brokerNode, TopicMetadata topicMetadata, int batchSize, long timeout, TimeUnit timeoutUnit, PollerListener listener) {
        ConsumerPolicy consumerPolicy = topicMetadata.getConsumerPolicy();
        timeout = timeoutUnit.toMillis(timeout);
        String topic = topicMetadata.getTopic();
        String app = config.getAppFullName();
        long ackTimeout = (config.getAckTimeout() == ConsumerConfig.NONE_ACK_TIMEOUT ? consumerPolicy.getAckTimeout() : config.getAckTimeout());

        if (listener == null) {
            FetchMessageData fetchMessageData = messageFetcher.fetch(brokerNode, topic, app, batchSize, timeout, ackTimeout, config.getLongPollTimeout());
            return handleFetchMessageData(topic, app, fetchMessageData);
        } else {
            messageFetcher.asyncFetch(brokerNode, topic, app, batchSize, timeout, ackTimeout, config.getLongPollTimeout(), new FetchListener() {
                @Override
                public void onMessage(FetchMessageData fetchMessageData) {
                    try {
                        List<ConsumeMessage> messages = handleFetchMessageData(topic, app, fetchMessageData);
                        listener.onMessage(messages);
                    } catch (Exception e) {
                        listener.onException(e);
                    }
                }

                @Override
                public void onException(Throwable cause) {
                    listener.onException(cause);
                }
            });
            return null;
        }
    }

    public List<ConsumeMessage> fetchPartition(BrokerNode brokerNode, String topic, short partition, int batchSize, long timeout, TimeUnit timeoutUnit, PollerListener listener) {
        return fetchPartition(brokerNode, topic, partition, FETCH_PARTITION_NONE_INDEX, batchSize, timeout, timeoutUnit, listener);
    }

    public List<ConsumeMessage> fetchPartition(BrokerNode brokerNode, TopicMetadata topicMetadata, short partition, int batchSize, long timeout, TimeUnit timeoutUnit, PollerListener listener) {
        return fetchPartition(brokerNode, topicMetadata, partition, FETCH_PARTITION_NONE_INDEX, batchSize, timeout, timeoutUnit, listener);
    }

    public List<ConsumeMessage> fetchPartition(BrokerNode brokerNode, String topic, short partition, long index, int batchSize, long timeout, TimeUnit timeoutUnit, PollerListener listener) {
        Preconditions.checkArgument(StringUtils.isNotBlank(topic), "topic not blank");

        TopicMetadata topicMetadata = getAndCheckTopicMetadata(topic);
        return fetchPartition(brokerNode, topicMetadata, partition, index, batchSize, timeout, timeoutUnit, listener);
    }

    public List<ConsumeMessage> fetchPartition(BrokerNode brokerNode, TopicMetadata topicMetadata, short partition, long index,
                                               int batchSize, long timeout, TimeUnit timeoutUnit, PollerListener listener) {
        Preconditions.checkArgument(topicMetadata != null, "topicMetadata not null");
        Preconditions.checkArgument(timeoutUnit != null, "timeoutUnit not null");

        TraceCaller caller = buildTraceCaller(topicMetadata);
        try {
            List<ConsumeMessage> consumeMessages = doFetchPartition(brokerNode, topicMetadata, partition, index, batchSize, timeout, timeoutUnit, listener);
            caller.end();
            return consumeMessages;
        } catch (Exception e) {
            caller.error();
            if (e instanceof ConsumerException) {
                throw (ConsumerException) e;
            } else {
                throw new ConsumerException(e);
            }
        }
    }

    protected List<ConsumeMessage> doFetchPartition(BrokerNode brokerNode, TopicMetadata topicMetadata, short partition,
                                                    long index, int batchSize, long timeout, TimeUnit timeoutUnit, PollerListener listener) {
        timeout = timeoutUnit.toMillis(timeout);
        String topic = topicMetadata.getTopic();
        String app = config.getAppFullName();

        if (listener == null) {
            FetchMessageData fetchMessageData = (index == FETCH_PARTITION_NONE_INDEX ?
                    messageFetcher.fetchPartition(brokerNode, topic, app, partition, batchSize, timeout):
                    messageFetcher.fetchPartition(brokerNode, topic, app, partition, index, batchSize, timeout));
            return handleFetchMessageData(topic, app, fetchMessageData);
        } else {
            PartitionFetchListener partitionFetchListenerAdapter = new PartitionFetchListener() {
                @Override
                public void onMessage(FetchMessageData fetchMessageData) {
                    try {
                        List<ConsumeMessage> consumeMessages = handleFetchMessageData(topic, app, fetchMessageData);
                        listener.onMessage(consumeMessages);
                    } catch (Exception e) {
                        listener.onException(e);
                    }
                }

                @Override
                public void onException(Throwable cause) {
                    listener.onException(cause);
                }
            };

            if (index == FETCH_PARTITION_NONE_INDEX) {
                messageFetcher.fetchPartitionAsync(brokerNode, topic, app, partition, batchSize, timeout, partitionFetchListenerAdapter);
            } else {
                messageFetcher.fetchPartitionAsync(brokerNode, topic, app, partition, index, batchSize, timeout, partitionFetchListenerAdapter);
            }
            return null;
        }
    }

    protected List<ConsumeMessage> handleFetchMessageData(String topic, String app, FetchMessageData fetchMessageData) {
        if (fetchMessageData == null) {
            throw new ConsumerException(JoyQueueCode.CN_UNKNOWN_ERROR.getMessage(), JoyQueueCode.CN_UNKNOWN_ERROR.getCode());
        }

        JoyQueueCode code = fetchMessageData.getCode();
        if (code.equals(JoyQueueCode.SUCCESS)) {
            return fetchMessageData.getMessages();
        }

        switch (code) {
            case CN_NO_PERMISSION:
            case CN_SERVICE_NOT_AVAILABLE:
            case FW_FETCH_TOPIC_MESSAGE_BROKER_NOT_LEADER: {
                logger.warn("fetch message error, no permission, topic: {}", topic);
                clusterManager.updateTopicMetadata(topic, app);
                break;
            }
            case FW_GET_MESSAGE_TOPIC_NOT_READ:
            case FW_FETCH_TOPIC_MESSAGE_PAUSED: {
                logger.debug("fetch message error, not read or paused, topic: {}", topic);
                break;
            }
            case FW_FETCH_MESSAGE_INDEX_OUT_OF_RANGE:
            case SE_INDEX_OVERFLOW:
            case SE_INDEX_UNDERFLOW: {
                logger.warn("fetch message index out of range, reset index, topic: {}, app: {}", topic, app);
                throw new ConsumerException(code.getMessage(), code.getCode());
            }
            case FW_TOPIC_NOT_EXIST: {
                logger.debug("fetch message error, topic not exist, topic: {}", topic);
                throw new ConsumerException(code.getMessage(), code.getCode());
            }
            case FW_BROKER_NOT_READABLE: {
                logger.debug("fetch message error, broker not readable, topic: {}", topic);
                break;
            }
            default: {
                logger.error("fetch message error, topic: {}, code: {}, error: {}", topic, code, code.getMessage());
                break;
            }
        }

        return Collections.emptyList();
    }

    protected TraceCaller buildTraceCaller(TopicMetadata topicMetadata) {
        return TraceBuilder.newInstance()
                .topic(topicMetadata.getTopic())
                .app(config.getAppFullName())
                .namespace(nameServerConfig.getNamespace())
                .type(TraceType.CONSUMER_FETCH)
                .begin();
    }

    public BrokerAssignments filterRegionBrokers(TopicMetadata topicMetadata, BrokerAssignments brokerAssignments) {
        if (!topicMetadata.getConsumerPolicy().getNearby() || CollectionUtils.isEmpty(brokerAssignments.getAssignments())) {
            return brokerAssignments;
        }
        List<BrokerAssignment> newAssignments = null;
        for (BrokerAssignment brokerAssignment : brokerAssignments.getAssignments()) {
            if (brokerAssignment.getBroker().isNearby()) {
                continue;
            }
            if (newAssignments == null) {
                newAssignments = Lists.newArrayList(brokerAssignments.getAssignments());
            }
            newAssignments.remove(brokerAssignment);
        }
        if (newAssignments == null) {
            return brokerAssignments;
        }
        return new BrokerAssignments(newAssignments);
    }

    public BrokerAssignments filterNotAvailableBrokers(BrokerAssignments brokerAssignments) {
        if (CollectionUtils.isEmpty(brokerAssignments.getAssignments())) {
            return brokerAssignments;
        }
        List<BrokerAssignment> newAssignments = null;
        for (BrokerAssignment brokerAssignment : brokerAssignments.getAssignments()) {
            ConsumerClient client = consumerClientManager.tryGetClient(brokerAssignment.getBroker());
            if (client == null || client.getState().equals(ClientState.CONNECTED)) {
                continue;
            }
            if (newAssignments == null) {
                newAssignments = Lists.newArrayList(brokerAssignments.getAssignments());
            }
            newAssignments.remove(brokerAssignment);
        }
        if (newAssignments == null) {
            return brokerAssignments;
        }
        return new BrokerAssignments(newAssignments);
    }

    public BrokerLoadBalance getBrokerLoadBalance(String topic) {
        return brokerLoadBalanceManager.getBrokerLoadBalance(topic, config.getLoadBalanceType());
    }

    public List<ConsumeMessage> buildPollEmptyResult(PollerListener listener) {
        if (listener == null) {
            return Collections.emptyList();
        } else {
            listener.onMessage(Collections.emptyList());
            return null;
        }
    }

    public BrokerAssignments buildAllBrokerAssignments(TopicMetadata topicMetadata) {
        return BrokerAssignmentConverter.convertBrokerAssignments(topicMetadata);
    }

    public TopicMetadata getAndCheckTopicMetadata(String topic) {
        TopicMetadata topicMetadata = clusterManager.fetchTopicMetadata(getTopicFullName(topic), config.getAppFullName());
        if (topicMetadata == null) {
            throw new ConsumerException(String.format("topic %s does not exist", topic), JoyQueueCode.FW_TOPIC_NOT_EXIST.getCode());
        }
        if (topicMetadata.getConsumerPolicy() == null) {
            throw new ConsumerException(String.format("topic %s consumer %s does not exist", topic, config.getAppFullName()), JoyQueueCode.FW_CONSUMER_NOT_EXISTS.getCode());
        }
        return topicMetadata;
    }

    public String getTopicFullName(String topic) {
        return NameServerHelper.getTopicFullName(topic, nameServerConfig);
    }
}
