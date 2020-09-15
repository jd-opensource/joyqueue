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
package org.joyqueue.client.internal.producer.support;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.joyqueue.client.internal.cluster.ClusterManager;
import org.joyqueue.client.internal.exception.ClientException;
import org.joyqueue.client.internal.metadata.domain.PartitionMetadata;
import org.joyqueue.client.internal.metadata.domain.PartitionNode;
import org.joyqueue.client.internal.metadata.domain.TopicMetadata;
import org.joyqueue.client.internal.metadata.exception.MetadataException;
import org.joyqueue.client.internal.nameserver.NameServerConfig;
import org.joyqueue.client.internal.nameserver.helper.NameServerHelper;
import org.joyqueue.client.internal.producer.MessageSender;
import org.joyqueue.client.internal.producer.PartitionSelector;
import org.joyqueue.client.internal.producer.callback.AsyncBatchProduceCallback;
import org.joyqueue.client.internal.producer.callback.AsyncBatchProduceCallbackAdapter;
import org.joyqueue.client.internal.producer.callback.AsyncBatchSendCallback;
import org.joyqueue.client.internal.producer.callback.AsyncProduceCallback;
import org.joyqueue.client.internal.producer.checker.ProduceMessageChecker;
import org.joyqueue.client.internal.producer.config.ProducerConfig;
import org.joyqueue.client.internal.producer.domain.ProduceMessage;
import org.joyqueue.client.internal.producer.domain.SendBatchResultData;
import org.joyqueue.client.internal.producer.domain.SendResult;
import org.joyqueue.client.internal.producer.exception.NeedRetryException;
import org.joyqueue.client.internal.producer.exception.ProducerException;
import org.joyqueue.client.internal.producer.helper.ProducerHelper;
import org.joyqueue.client.internal.producer.interceptor.ProduceContext;
import org.joyqueue.client.internal.producer.interceptor.ProducerInterceptor;
import org.joyqueue.client.internal.producer.interceptor.ProducerInterceptorManager;
import org.joyqueue.client.internal.producer.interceptor.ProducerInvocation;
import org.joyqueue.client.internal.producer.interceptor.ProducerInvoker;
import org.joyqueue.client.internal.producer.transport.ProducerClient;
import org.joyqueue.client.internal.producer.transport.ProducerClientManager;
import org.joyqueue.client.internal.transport.ClientState;
import org.joyqueue.domain.ProducerPolicy;
import org.joyqueue.domain.QosLevel;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.network.domain.BrokerNode;
import org.joyqueue.toolkit.retry.RetryPolicy;
import org.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * MessageProducerInner
 *
 * author: gaohaoxiang
 * date: 2019/1/4
 */
public class MessageProducerInner extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(MessageProducerInner.class);

    private ProducerConfig config;
    private NameServerConfig nameServerConfig;
    private MessageSender messageSender;
    private ClusterManager clusterManager;
    private ProducerClientManager producerClientManager;
    private PartitionSelectorManager partitionSelectorManager;
    private ProducerInterceptorManager producerInterceptorManager;

    public MessageProducerInner(ProducerConfig config, NameServerConfig nameServerConfig, MessageSender messageSender, ClusterManager clusterManager, ProducerClientManager producerClientManager) {
        this(config, nameServerConfig, messageSender, clusterManager, producerClientManager, new ProducerInterceptorManager());
    }

    public MessageProducerInner(ProducerConfig config, NameServerConfig nameServerConfig,
                                MessageSender messageSender, ClusterManager clusterManager,
                                ProducerClientManager producerClientManager, ProducerInterceptorManager producerInterceptorManager) {
        this.config = config;
        this.nameServerConfig = nameServerConfig;
        this.messageSender = messageSender;
        this.clusterManager = clusterManager;
        this.producerClientManager = producerClientManager;
        this.producerInterceptorManager = producerInterceptorManager;
    }

    @Override
    protected void validate() throws Exception {
        partitionSelectorManager = new PartitionSelectorManager();
    }

    public synchronized void addInterceptor(ProducerInterceptor interceptor) {
        producerInterceptorManager.addInterceptor(interceptor);
    }

    public synchronized void removeInterceptor(ProducerInterceptor interceptor) {
        producerInterceptorManager.removeInterceptor(interceptor);
    }

    public SendResult send(ProduceMessage message, String txId, long timeout, TimeUnit timeoutUnit, boolean isOneway, boolean failover, AsyncProduceCallback callback) {
        AsyncBatchProduceCallback asyncBatchProduceCallback = (callback == null ? null : new AsyncBatchProduceCallbackAdapter(callback));
        List<SendResult> sendResults = batchSend(Lists.newArrayList(message), txId, timeout, timeoutUnit, isOneway, failover, asyncBatchProduceCallback);
        if (CollectionUtils.isEmpty(sendResults)) {
            return null;
        }
        return sendResults.get(0);
    }

    public List<SendResult> batchSend(List<ProduceMessage> messages, String txId, long timeout, TimeUnit timeoutUnit, boolean isOneway, boolean failover, AsyncBatchProduceCallback callback) {
        Preconditions.checkArgument(timeoutUnit != null, "timeoutUnit not null");
        ProduceMessageChecker.checkMessages(messages, config);

        return doBatchSend(messages, txId, timeout, timeoutUnit, isOneway, failover, callback);
    }

    public List<SendResult> doBatchSend(List<ProduceMessage> messages, String txId, long timeout, TimeUnit timeoutUnit, boolean isOneway, boolean failover, AsyncBatchProduceCallback callback) {
        TopicMetadata topicMetadata = getAndCheckTopicMetadata(messages.get(0).getTopic());
        List<BrokerNode> brokerNodes = getAvailableBrokers(topicMetadata);

        return doBatchSend(messages, topicMetadata, brokerNodes,
                txId, timeout, timeoutUnit, isOneway, failover, callback);
    }

    public List<SendResult> doBatchSend(List<ProduceMessage> messages, TopicMetadata topicMetadata, List<BrokerNode> brokers,
                                        String txId, long timeout, TimeUnit timeoutUnit, boolean isOneway, boolean failover, AsyncBatchProduceCallback callback) {

        try {
            return new ProducerInvocation(config, nameServerConfig, topicMetadata, messages, producerInterceptorManager, new ProducerInvoker() {
                @Override
                public List<SendResult> invoke(ProduceContext context) {
                    return doBatchSendInternal(messages, topicMetadata, brokers, txId, timeout, timeoutUnit, isOneway, failover, callback);
                }

                @Override
                public List<SendResult> reject(ProduceContext context) {
                    throw new ProducerException("reject send", JoyQueueCode.CN_UNKNOWN_ERROR.getCode());
                }
            }).invoke();
        } catch (Exception e) {
            if (e instanceof ProducerException) {
                throw (ProducerException) e;
            } else {
                throw new ProducerException(e);
            }
        }
    }

    protected List<SendResult> doBatchSendInternal(List<ProduceMessage> messages, TopicMetadata topicMetadata, List<BrokerNode> brokerNodes,
                                                   String txId, long timeout, TimeUnit timeoutUnit, boolean isOneway, boolean failover, AsyncBatchProduceCallback callback) {

        ProducerPolicy producerPolicy = topicMetadata.getProducerPolicy();

        String topic = topicMetadata.getTopic();
        String app = config.getApp();
        long produceTimeout = (config.getProduceTimeout() == ProducerConfig.NONE_PRODUCE_TIMEOUT ? producerPolicy.getTimeOut() : config.getProduceTimeout());
        timeout = timeoutUnit.toMillis(timeout);
        failover = failover && isFailover(messages);

        RetryPolicy retryPolicy = config.getRetryPolicy();
        int retryTimes = 0;
        int retryLimit = (failover ? retryPolicy.getMaxRetrys() : 0);
        PartitionNode partitionNode = dispatchPartitions(messages, topicMetadata, brokerNodes);
        Exception lastException = null;
        List<SendResult> result = null;

        for (int i = 0; i <= retryLimit; i++) {
            if (retryTimes != 0 && failover) {
                if (retryTimes == 1) {
                    brokerNodes = Lists.newArrayList(brokerNodes);
                }
                ProducerHelper.clearPartitions(messages);
                brokerNodes.remove(partitionNode.getPartitionMetadata().getLeader());
                partitionNode = dispatchPartitions(messages, topicMetadata, brokerNodes);
            }

            PartitionNode.PartitionNodeTracer partitionTracer = partitionNode.begin();
            try {
                result = doSendBatchMessage(partitionNode.getPartitionMetadata().getLeader(), topic, app, messages, txId, config.getQosLevel(), produceTimeout, timeout, isOneway, callback);
                partitionTracer.end();
                break;
            } catch (MetadataException e) {
                lastException = e;
                retryTimes++;
                topicMetadata = getAndCheckTopicMetadata(topicMetadata.getTopic());
                partitionTracer.error();
                logger.debug("send message exception, topic: {}, app:{}, messages: {}", topic, app, messages, e);
            } catch (NeedRetryException e) {
                lastException = new ProducerException(e.getMessage(), e.getCode(), e.getCause());
                retryTimes++;
                partitionTracer.error();
                logger.debug("send message exception, topic: {}, app:{}, messages: {}", topic, app, messages, e);
            } catch (ClientException e) {
                lastException = e;
                retryTimes++;
                partitionTracer.error();
                logger.debug("send message exception, topic: {}, app:{}, messages: {}", topic, app, messages, e);
            } catch (Exception e) {
                partitionTracer.error();
                logger.error("send message exception, topic: {}, app:{}, messages: {}", topic, app, messages, e);
                if (e instanceof ProducerException) {
                    throw (ProducerException) e;
                } else {
                    throw new ProducerException(e);
                }
            }
        }

        if (retryTimes > retryPolicy.getMaxRetrys()) {
            if (lastException instanceof ProducerException) {
                throw (ProducerException) lastException;
            }
            throw new ProducerException(lastException);
        }
        if (retryTimes != 0) {
            logger.warn("send message success, retry {} times, topic: {}, app: {}, error: {}", retryTimes, topic, app, lastException.getMessage());
        }
        return result;
    }

    protected List<SendResult> doSendBatchMessage(BrokerNode brokerNode, String topic, String app, List<ProduceMessage> messages, String txId, QosLevel qosLevel,
                                                  long produceTimeout, long timeout, boolean isOneway, AsyncBatchProduceCallback callback) {

        if (logger.isDebugEnabled()) {
            logger.debug("batch send message, broker: {}, topic: {}, app: {}, messages: {}, txId: {}, qosLevel: {}", brokerNode, topic, app, messages, txId, qosLevel);
        }

        if (isOneway) {
            messageSender.batchSendOneway(brokerNode, topic, app, txId, messages, qosLevel, produceTimeout, timeout);
            return null;
        }

        if (callback == null) {
            SendBatchResultData sendBatchResultData = messageSender.batchSend(brokerNode, topic, app, txId, messages, qosLevel, produceTimeout, timeout);
            return handleSendBatchResultData(topic, app, sendBatchResultData);
        } else {
            messageSender.batchSendAsync(brokerNode, topic, app, txId, messages, qosLevel, produceTimeout, timeout, new AsyncBatchSendCallback() {
                @Override
                public void onSuccess(List<ProduceMessage> messages, SendBatchResultData result) {
                    if (result.getCode().equals(JoyQueueCode.SUCCESS)) {
                        callback.onSuccess(messages, result.getResult());
                    } else {
                        callback.onException(messages, new ProducerException(result.getCode().getMessage(), result.getCode().getCode()));
                    }
                }

                @Override
                public void onException(List<ProduceMessage> messages, Throwable cause) {
                    callback.onException(messages, cause);
                }
            });
            return null;
        }
    }

    protected List<SendResult> handleSendBatchResultData(String topic, String app, SendBatchResultData sendBatchResultData) {
        if (sendBatchResultData == null) {
            throw new ProducerException(JoyQueueCode.CN_UNKNOWN_ERROR.getMessage(), JoyQueueCode.CN_UNKNOWN_ERROR.getCode());
        }

        JoyQueueCode code = sendBatchResultData.getCode();
        if (code.equals(JoyQueueCode.SUCCESS)) {
            return sendBatchResultData.getResult();
        }

        switch (code) {
            case CN_NO_PERMISSION:
            case CN_SERVICE_NOT_AVAILABLE:
            case FW_PRODUCE_MESSAGE_BROKER_NOT_LEADER: {
                logger.debug("send message error, no permission, topic: {}", topic);
                clusterManager.updateTopicMetadata(topic, app);
                throw new MetadataException(code.getMessage(), code.getCode());
            }
            case FW_PUT_MESSAGE_TOPIC_NOT_WRITE: {
                logger.debug("send message error, topic not write, topic: {}", topic);
                break;
            }
            case FW_TOPIC_NOT_EXIST: {
                logger.debug("send message error, topic not exist, topic: {}", topic);
                throw new ProducerException(code.getMessage(), code.getCode());
            }
            case FW_BROKER_NOT_WRITABLE: {
                logger.debug("send message error, broker not writable, topic: {}", topic);
                break;
            }
            default: {
                logger.error("send message error, topic: {}, code: {}, error: {}", topic, code, code.getMessage());
                throw new NeedRetryException(code.getMessage(), code.getCode());
            }
        }
        throw new NeedRetryException(code.getMessage(), code.getCode());
    }

    public TopicMetadata getAndCheckTopicMetadata(String topic) {
        TopicMetadata topicMetadata = clusterManager.fetchTopicMetadata(getTopicFullName(topic), config.getApp());
        if (topicMetadata == null) {
            throw new ProducerException(String.format("topic %s is not exist", topic), JoyQueueCode.FW_TOPIC_NOT_EXIST.getCode());
        }
        if (topicMetadata.getProducerPolicy() == null) {
            throw new ProducerException(String.format("topic %s producer %s is not exist", topic, nameServerConfig.getApp()), JoyQueueCode.FW_PRODUCER_NOT_EXISTS.getCode());
        }
        return topicMetadata;
    }

    public String getTopicFullName(String topic) {
        return NameServerHelper.getTopicFullName(topic, nameServerConfig);
    }

    public List<BrokerNode> getRegionBrokers(TopicMetadata topicMetadata) {
        if (topicMetadata.getProducerPolicy().getNearby()) {
            return topicMetadata.getWritableNearbyBrokers();
        } else {
            return topicMetadata.getWritableBrokers();
        }
    }

    public List<BrokerNode> filterNotAvailableBrokers(List<BrokerNode> brokerNodes) {
        if (CollectionUtils.isEmpty(brokerNodes)) {
            return brokerNodes;
        }
        List<BrokerNode> newBrokerNodes = null;
        for (BrokerNode brokerNode : brokerNodes) {
            ProducerClient client = producerClientManager.tryGetClient(brokerNode);
            if (client == null || client.getState().equals(ClientState.CONNECTED)) {
                continue;
            }
            if (newBrokerNodes == null) {
                newBrokerNodes = Lists.newArrayList(brokerNodes);
            }
            newBrokerNodes.remove(brokerNode);
        }
        if (newBrokerNodes == null) {
            return brokerNodes;
        }
        return newBrokerNodes;
    }

    public List<BrokerNode> getAvailableBrokers(TopicMetadata topicMetadata) {
        List<BrokerNode> brokers = getRegionBrokers(topicMetadata);
        return filterNotAvailableBrokers(brokers);
    }

    public List<PartitionMetadata> getBrokerPartitions(TopicMetadata topicMetadata, List<BrokerNode> brokerNodes) {
        if (topicMetadata.getBrokers().equals(brokerNodes)) {
            return topicMetadata.getPartitions();
        }
        List<PartitionMetadata> result = Lists.newArrayListWithCapacity(topicMetadata.getPartitions().size());
        for (BrokerNode brokerNode : brokerNodes) {
            List<PartitionMetadata> brokerPartitions = topicMetadata.getBrokerPartitions(brokerNode.getId());
            if (brokerPartitions != null) {
                result.addAll(brokerPartitions);
            }
        }
        return result;
    }

    public PartitionNode dispatchPartitions(List<ProduceMessage> messages, TopicMetadata topicMetadata, List<BrokerNode> brokerNodes) {
        if (CollectionUtils.isEmpty(brokerNodes)) {
            throw new ProducerException(String.format("no broker available, topic: %s, messages: %s", topicMetadata.getTopic(), messages), JoyQueueCode.FW_TOPIC_NO_PARTITIONGROUP.getCode());
        }

        PartitionSelector partitionSelector = partitionSelectorManager.getPartitionSelector(topicMetadata.getTopic(), config.getSelectorType());
        PartitionNode partitionNode = ProducerHelper.dispatchPartitions(messages, topicMetadata, brokerNodes, partitionSelector);

        if (partitionNode == null || partitionNode.getPartitionMetadata().getLeader() == null) {
            throw new ProducerException(String.format("partition not available, topic: %s, messages: %s", topicMetadata.getTopic(), messages), JoyQueueCode.FW_TOPIC_NO_PARTITIONGROUP.getCode());
        }
        ProducerHelper.setPartitions(messages, partitionNode.getPartitionMetadata().getId());
        return partitionNode;
    }

    public boolean isFailover(List<ProduceMessage> messages) {
        return (messages.get(0).getPartition() == ProduceMessage.NONE_PARTITION);
    }
}