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
import com.google.common.collect.Maps;
import org.joyqueue.client.internal.exception.ClientException;
import org.joyqueue.client.internal.producer.MessageSender;
import org.joyqueue.client.internal.producer.callback.AsyncBatchSendCallback;
import org.joyqueue.client.internal.producer.callback.AsyncMultiBatchSendCallback;
import org.joyqueue.client.internal.producer.callback.AsyncSendCallback;
import org.joyqueue.client.internal.producer.config.SenderConfig;
import org.joyqueue.client.internal.producer.converter.MessageSenderConverter;
import org.joyqueue.client.internal.producer.domain.FetchFeedbackData;
import org.joyqueue.client.internal.producer.domain.ProduceMessage;
import org.joyqueue.client.internal.producer.domain.SendBatchResultData;
import org.joyqueue.client.internal.producer.domain.SendPrepareResult;
import org.joyqueue.client.internal.producer.domain.SendResultData;
import org.joyqueue.client.internal.producer.transport.ProducerClient;
import org.joyqueue.client.internal.producer.transport.ProducerClientGroup;
import org.joyqueue.client.internal.producer.transport.ProducerClientManager;
import org.joyqueue.client.internal.transport.ConnectionState;
import org.joyqueue.domain.QosLevel;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.network.command.FetchProduceFeedbackResponse;
import org.joyqueue.network.command.ProduceMessageAckData;
import org.joyqueue.network.command.ProduceMessageCommitResponse;
import org.joyqueue.network.command.ProduceMessageData;
import org.joyqueue.network.command.ProduceMessagePrepareResponse;
import org.joyqueue.network.command.ProduceMessageResponse;
import org.joyqueue.network.command.ProduceMessageRollbackResponse;
import org.joyqueue.network.command.TxStatus;
import org.joyqueue.network.domain.BrokerNode;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.CommandCallback;
import org.joyqueue.toolkit.service.Service;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * DefaultMessageSender
 *
 * author: gaohaoxiang
 * date: 2018/12/10
 */
public class DefaultMessageSender extends Service implements MessageSender {

    protected static final Logger logger = LoggerFactory.getLogger(DefaultMessageSender.class);

    private ProducerClientManager producerClientManager;
    private SenderConfig config;
    private ConnectionState connectionState = new ConnectionState();

    public DefaultMessageSender(ProducerClientManager producerClientManager, SenderConfig config) {
        Preconditions.checkArgument(producerClientManager != null, "producerClientManager not null");

        this.producerClientManager = producerClientManager;
        this.config = config;
    }

    @Override
    public SendResultData send(BrokerNode brokerNode, String topic, String app, String txId, ProduceMessage message, QosLevel qosLevel, long produceTimeout, long timeout) {
        List<ProduceMessage> messages = Lists.newArrayList(message);
        SendBatchResultData sendBatchResultData = batchSend(brokerNode, topic, app, txId, messages, qosLevel, produceTimeout, timeout);

        SendResultData sendResultData = new SendResultData();
        sendResultData.setCode(sendBatchResultData.getCode());
        if (CollectionUtils.isNotEmpty(sendBatchResultData.getResult())) {
            sendResultData.setResult(sendBatchResultData.getResult().get(0));
        }
        return sendResultData;
    }

    @Override
    public SendBatchResultData batchSend(BrokerNode brokerNode, String topic, String app, String txId, List<ProduceMessage> messages, QosLevel qosLevel, long produceTimeout, long timeout) {
        Map<String, List<ProduceMessage>> messageMap = Maps.newHashMapWithExpectedSize(1);
        messageMap.put(topic, messages);

        Map<String, SendBatchResultData> produceBatchResultDataMap = batchSend(brokerNode, app, txId, messageMap, qosLevel, produceTimeout, timeout);
        return produceBatchResultDataMap.get(topic);
    }

    @Override
    public void sendAsync(BrokerNode brokerNode, String topic,
                          String app, String txId,
                          final ProduceMessage message, QosLevel qosLevel,
                          long produceTimeout, long timeout,
                          final AsyncSendCallback callback) {
        List<ProduceMessage> messages = Lists.newArrayList(message);
        batchSendAsync(brokerNode, topic, app, txId, messages, qosLevel, produceTimeout, timeout, new AsyncBatchSendCallback() {
            @Override
            public void onSuccess(List<ProduceMessage> messages, SendBatchResultData sendBatchResultData) {
                SendResultData sendResultData = new SendResultData();
                sendResultData.setCode(sendBatchResultData.getCode());
                if (CollectionUtils.isNotEmpty(sendBatchResultData.getResult())) {
                    sendResultData.setResult(sendBatchResultData.getResult().get(0));
                }
                callback.onSuccess(message, sendResultData);
            }

            @Override
            public void onException(List<ProduceMessage> messages, Throwable cause) {
                callback.onException(message, cause);
            }
        });
    }

    @Override
    public void batchSendAsync(BrokerNode brokerNode, final String topic,
                               String app, String txId,
                               List<ProduceMessage> messages, QosLevel qosLevel,
                               long produceTimeout, long timeout,
                               final AsyncBatchSendCallback callback) {
        Map<String, List<ProduceMessage>> messageMap = Maps.newHashMapWithExpectedSize(1);
        messageMap.put(topic, messages);

        batchSendAsync(brokerNode, app, txId, messageMap, qosLevel, produceTimeout, timeout, new AsyncMultiBatchSendCallback() {
            @Override
            public void onSuccess(Map<String, List<ProduceMessage>> messages, Map<String, SendBatchResultData> result) {
                SendBatchResultData sendBatchResultData = result.get(topic);
                callback.onSuccess(messages.get(topic), sendBatchResultData);
            }

            @Override
            public void onException(Map<String, List<ProduceMessage>> messages, Throwable cause) {
                callback.onException(messages.get(topic), cause);
            }
        });
    }

    @Override
    public CompletableFuture<SendBatchResultData> batchSendAsync(BrokerNode brokerNode, final String topic,
                                                                 String app, String txId,
                                                                 List<ProduceMessage> messages, QosLevel qosLevel,
                                                                 long produceTimeout, long timeout) {
        CompletableFuture<SendBatchResultData> future = new CompletableFuture<>();
        Map<String, List<ProduceMessage>> messageMap = Maps.newHashMapWithExpectedSize(1);
        messageMap.put(topic, messages);

        batchSendAsync(brokerNode, app, txId, messageMap, qosLevel, produceTimeout, timeout, new AsyncMultiBatchSendCallback() {
            @Override
            public void onSuccess(Map<String, List<ProduceMessage>> messages, Map<String, SendBatchResultData> result) {
                SendBatchResultData sendBatchResultData = result.get(topic);
                future.complete(sendBatchResultData);
            }

            @Override
            public void onException(Map<String, List<ProduceMessage>> messages, Throwable cause) {
                future.completeExceptionally(cause);
            }
        });
        return future;
    }

    @Override
    public void sendOneway(BrokerNode brokerNode, String topic, String app, String txId, ProduceMessage message, QosLevel qosLevel, long produceTimeout, long timeout) {
        List<ProduceMessage> messages = Lists.newArrayList(message);
        batchSendOneway(brokerNode, topic, app, txId, messages, qosLevel, produceTimeout, timeout);
    }

    @Override
    public void batchSendOneway(BrokerNode brokerNode, String topic, String app, String txId, List<ProduceMessage> messages, QosLevel qosLevel, long produceTimeout, long timeout) {
        Map<String, List<ProduceMessage>> messageMap = Maps.newHashMapWithExpectedSize(1);
        messageMap.put(topic, messages);

        batchSendOneway(brokerNode, app, txId, messageMap, qosLevel, produceTimeout, timeout);
    }

    @Override
    public void batchSendOneway(BrokerNode brokerNode, String app, String txId, Map<String, List<ProduceMessage>> messages, QosLevel qosLevel, long produceTimeout, long timeout) {
        checkState();
        Map<String, ProduceMessageData> data = Maps.newHashMap();

        for (Map.Entry<String, List<ProduceMessage>> entry : messages.entrySet()) {
            String topic = entry.getKey();
            ProduceMessageData produceMessageData = MessageSenderConverter.convertToProduceMessageData(topic, app, txId, entry.getValue(), qosLevel, produceTimeout,
                    config.isCompress(), config.getCompressThreshold(), config.getCompressType(), config.isBatch());
            data.put(topic, produceMessageData);
        }

        ProducerClient client = producerClientManager.getOrCreateClient(brokerNode);
        handleAddProducers(brokerNode, messages.keySet(), app, client);
        client.produceMessageOneway(app, data, timeout);
    }

    @Override
    public Map<String, SendBatchResultData> batchSend(BrokerNode brokerNode, String app,
                                                      String txId, Map<String, List<ProduceMessage>> messages,
                                                      QosLevel qosLevel, long produceTimeout,
                                                      long timeout) {
        checkState();
        Map<String, ProduceMessageData> data = Maps.newHashMap();

        for (Map.Entry<String, List<ProduceMessage>> entry : messages.entrySet()) {
            String topic = entry.getKey();
            ProduceMessageData produceMessageData = MessageSenderConverter.convertToProduceMessageData(topic, app, txId, entry.getValue(), qosLevel, produceTimeout,
                    config.isCompress(), config.getCompressThreshold(), config.getCompressType(), config.isBatch());
            data.put(topic, produceMessageData);
        }

        ProducerClient client = producerClientManager.getOrCreateClient(brokerNode);
        handleAddProducers(brokerNode, messages.keySet(), app, client);

        ProduceMessageResponse produceMessageResponse = client.produceMessage(app, data, timeout);
        Map<String, SendBatchResultData> result = Maps.newHashMap();
        for (Map.Entry<String, ProduceMessageAckData> entry : produceMessageResponse.getData().entrySet()) {
            result.put(entry.getKey(), MessageSenderConverter.convertToBatchResultData(entry.getKey(), app, entry.getValue()));
        }

        return result;
    }

    @Override
    public void batchSendAsync(BrokerNode brokerNode, final String app,
                               String txId, final Map<String, List<ProduceMessage>> messages,
                               QosLevel qosLevel, long produceTimeout,
                               long timeout, final AsyncMultiBatchSendCallback callback) {
        checkState();
        Map<String, ProduceMessageData> data = Maps.newHashMap();

        for (Map.Entry<String, List<ProduceMessage>> entry : messages.entrySet()) {
            String topic = entry.getKey();
            ProduceMessageData produceMessageData = MessageSenderConverter.convertToProduceMessageData(topic, app, txId, entry.getValue(), qosLevel, produceTimeout,
                    config.isCompress(), config.getCompressThreshold(), config.getCompressType(), config.isBatch());
            data.put(topic, produceMessageData);
        }

        ProducerClient client = producerClientManager.getOrCreateClient(brokerNode);
        handleAddProducers(brokerNode, messages.keySet(), app, client);

        try {
            client.asyncProduceMessage(app, data, timeout, new CommandCallback() {
                @Override
                public void onSuccess(Command request, Command response) {
                    ProduceMessageResponse produceMessageResponse = (ProduceMessageResponse) response.getPayload();
                    Map<String, SendBatchResultData> produceBatchResultData = Maps.newHashMap();
                    for (Map.Entry<String, ProduceMessageAckData> entry : produceMessageResponse.getData().entrySet()) {
                        produceBatchResultData.put(entry.getKey(), MessageSenderConverter.convertToBatchResultData(entry.getKey(), app, entry.getValue()));
                    }
                    callback.onSuccess(messages, produceBatchResultData);
                }

                @Override
                public void onException(Command request, Throwable cause) {
                    callback.onException(messages, cause);
                }
            });
        } catch (ClientException e) {
            callback.onException(messages, e);
        }
    }

    @Override
    public CompletableFuture<Map<String, SendBatchResultData>> batchSendAsync(BrokerNode brokerNode, String app,
                                                                              String txId, Map<String, List<ProduceMessage>> messages,
                                                                              QosLevel qosLevel, long produceTimeout, long timeout) {
        CompletableFuture<Map<String, SendBatchResultData>> future = new CompletableFuture<>();
        batchSendAsync(brokerNode, app, txId, messages, qosLevel, produceTimeout, timeout, new AsyncMultiBatchSendCallback() {
            @Override
            public void onSuccess(Map<String, List<ProduceMessage>> messages, Map<String, SendBatchResultData> result) {
                future.complete(result);
            }

            @Override
            public void onException(Map<String, List<ProduceMessage>> messages, Throwable cause) {
                future.completeExceptionally(cause);
            }
        });
        return future;
    }

    @Override
    public SendPrepareResult prepare(BrokerNode brokerNode, String topic, String app, String transactionId, long sequence, long transactionTimeout, long timeout) {
        checkState();
        ProducerClient client = producerClientManager.getOrCreateClient(brokerNode);
        handleAddProducers(brokerNode, Lists.newArrayList(topic), app, client);

        ProduceMessagePrepareResponse produceMessagePrepareResponse = client.produceMessagePrepare(topic, app, sequence, transactionId, transactionTimeout, timeout);
        return new SendPrepareResult(produceMessagePrepareResponse.getTxId(), produceMessagePrepareResponse.getCode());
    }

    @Override
    public JoyQueueCode commit(BrokerNode brokerNode, String topic, String app, String txId, long timeout) {
        checkState();
        ProducerClient client = producerClientManager.getOrCreateClient(brokerNode);
        handleAddProducers(brokerNode, Lists.newArrayList(topic), app, client);

        ProduceMessageCommitResponse produceMessageCommitResponse = client.produceMessageCommit(topic, app, txId, timeout);
        return produceMessageCommitResponse.getCode();
    }

    @Override
    public JoyQueueCode rollback(BrokerNode brokerNode, String topic, String app, String txId, long timeout) {
        checkState();
        ProducerClient client = producerClientManager.getOrCreateClient(brokerNode);
        handleAddProducers(brokerNode, Lists.newArrayList(topic), app, client);

        ProduceMessageRollbackResponse produceMessageRollbackResponse = client.produceMessageRollback(topic, app, txId, timeout);
        return produceMessageRollbackResponse.getCode();
    }

    @Override
    public FetchFeedbackData fetchFeedback(BrokerNode brokerNode, String topic, String app, TxStatus txStatus, int count, long longPollTimeout, long timeout) {
        checkState();
        ProducerClient client = producerClientManager.getOrCreateClient(brokerNode);
        handleAddProducers(brokerNode, Lists.newArrayList(topic), app, client);

        FetchProduceFeedbackResponse fetchProduceFeedbackResponse = client.fetchFeedback(topic, app, txStatus, count, longPollTimeout, timeout);
        return MessageSenderConverter.convertToFetchFeedbackData(topic, app, fetchProduceFeedbackResponse);
    }

    protected void checkState() {
        if (!isStarted()) {
            throw new ClientException("sender is not started", JoyQueueCode.CN_SERVICE_NOT_AVAILABLE.getCode());
        }
    }

    protected void handleAddProducers(BrokerNode brokerNode, Collection<String> topics, String app, ProducerClient client) {
        client.addProducers(topics, app);
        connectionState.addBrokerNode(brokerNode);
        connectionState.addTopics(topics);
        connectionState.addApp(app);
    }

    @Override
    protected void doStop() {
        handleRemoveProducers();
    }

    protected void handleRemoveProducers() {
        Set<BrokerNode> brokerNodes = connectionState.getBrokerNodes();
        Set<String> topics = connectionState.getTopics();
        Set<String> apps = connectionState.getApps();

        for (BrokerNode brokerNode : brokerNodes) {
            handleRemoveProducers(brokerNode, topics, apps);
        }
    }

    protected void handleRemoveProducers(BrokerNode brokerNode, Set<String> topics, Set<String> apps) {
        ProducerClientGroup clientGroup = producerClientManager.getClientGroup(brokerNode);
        if (clientGroup == null) {
            return;
        }
        for (String app : apps) {
            for (ProducerClient client : clientGroup.getClients()) {
                try {
                    client.removeProducers(topics, app);
                } catch (Exception e) {
                    logger.warn("remove producers exception, topics: {}, app: {}, exception: {}", topics, app, e.getMessage());
                    logger.debug("remove producers exception, topics: {}, app: {}", topics, app, e);
                }
            }
        }
    }
}