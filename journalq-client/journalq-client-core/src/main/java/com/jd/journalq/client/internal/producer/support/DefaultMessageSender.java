package com.jd.journalq.client.internal.producer.support;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jd.journalq.client.internal.exception.ClientException;
import com.jd.journalq.client.internal.producer.MessageSender;
import com.jd.journalq.client.internal.producer.callback.AsyncBatchSendCallback;
import com.jd.journalq.client.internal.producer.callback.AsyncMultiBatchSendCallback;
import com.jd.journalq.client.internal.producer.callback.AsyncSendCallback;
import com.jd.journalq.client.internal.producer.config.SenderConfig;
import com.jd.journalq.client.internal.producer.converter.MessageSenderConverter;
import com.jd.journalq.client.internal.producer.domain.FetchFeedbackData;
import com.jd.journalq.client.internal.producer.domain.ProduceMessage;
import com.jd.journalq.client.internal.producer.domain.SendBatchResultData;
import com.jd.journalq.client.internal.producer.domain.SendPrepareResult;
import com.jd.journalq.client.internal.producer.domain.SendResultData;
import com.jd.journalq.client.internal.producer.transport.ProducerClient;
import com.jd.journalq.client.internal.producer.transport.ProducerClientGroup;
import com.jd.journalq.client.internal.producer.transport.ProducerClientManager;
import com.jd.journalq.client.internal.transport.ConnectionState;
import com.jd.journalq.domain.QosLevel;
import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.network.command.FetchProduceFeedbackAck;
import com.jd.journalq.network.command.ProduceMessageAck;
import com.jd.journalq.network.command.ProduceMessageAckData;
import com.jd.journalq.network.command.ProduceMessageCommitAck;
import com.jd.journalq.network.command.ProduceMessageData;
import com.jd.journalq.network.command.ProduceMessagePrepareAck;
import com.jd.journalq.network.command.ProduceMessageRollbackAck;
import com.jd.journalq.network.command.TxStatus;
import com.jd.journalq.network.domain.BrokerNode;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.CommandCallback;
import com.jd.journalq.toolkit.concurrent.SimpleFuture;
import com.jd.journalq.toolkit.lang.Preconditions;
import com.jd.journalq.toolkit.service.Service;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * DefaultMessageSender
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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
    public void sendAsync(BrokerNode brokerNode, String topic, String app, String txId, final ProduceMessage message, QosLevel qosLevel, long produceTimeout, long timeout, final AsyncSendCallback callback) {
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
    public void batchSendAsync(BrokerNode brokerNode, final String topic, String app, String txId, List<ProduceMessage> messages, QosLevel qosLevel, long produceTimeout, long timeout, final AsyncBatchSendCallback callback) {
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
    public Future<SendBatchResultData> batchSendAsync(BrokerNode brokerNode, final String topic, String app, String txId, List<ProduceMessage> messages, QosLevel qosLevel, long produceTimeout, long timeout) {
        final SimpleFuture<SendBatchResultData> future = new SimpleFuture<SendBatchResultData>();
        Map<String, List<ProduceMessage>> messageMap = Maps.newHashMapWithExpectedSize(1);
        messageMap.put(topic, messages);

        batchSendAsync(brokerNode, app, txId, messageMap, qosLevel, produceTimeout, timeout, new AsyncMultiBatchSendCallback() {
            @Override
            public void onSuccess(Map<String, List<ProduceMessage>> messages, Map<String, SendBatchResultData> result) {
                SendBatchResultData sendBatchResultData = result.get(topic);
                future.setResponse(sendBatchResultData);
            }

            @Override
            public void onException(Map<String, List<ProduceMessage>> messages, Throwable cause) {
                future.setThrowable(cause);
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
    public Map<String, SendBatchResultData> batchSend(BrokerNode brokerNode, String app, String txId, Map<String, List<ProduceMessage>> messages, QosLevel qosLevel, long produceTimeout, long timeout) {
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

        ProduceMessageAck produceMessageAck = client.produceMessage(app, data, timeout);
        Map<String, SendBatchResultData> result = Maps.newHashMap();
        for (Map.Entry<String, ProduceMessageAckData> entry : produceMessageAck.getData().entrySet()) {
            result.put(entry.getKey(), MessageSenderConverter.convertToBatchResultData(entry.getKey(), app, entry.getValue()));
        }

        return result;
    }

    @Override
    public void batchSendAsync(BrokerNode brokerNode, final String app, String txId, final Map<String, List<ProduceMessage>> messages, QosLevel qosLevel, long produceTimeout, long timeout, final AsyncMultiBatchSendCallback callback) {
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
                    ProduceMessageAck produceMessageAck = (ProduceMessageAck) response.getPayload();
                    Map<String, SendBatchResultData> produceBatchResultData = Maps.newHashMap();
                    for (Map.Entry<String, ProduceMessageAckData> entry : produceMessageAck.getData().entrySet()) {
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
    public Future<Map<String, SendBatchResultData>> batchSendAsync(BrokerNode brokerNode, String app, String txId, Map<String, List<ProduceMessage>> messages, QosLevel qosLevel, long produceTimeout, long timeout) {
        final SimpleFuture<Map<String, SendBatchResultData>> future = new SimpleFuture<Map<String, SendBatchResultData>>();
        batchSendAsync(brokerNode, app, txId, messages, qosLevel, produceTimeout, timeout, new AsyncMultiBatchSendCallback() {
            @Override
            public void onSuccess(Map<String, List<ProduceMessage>> messages, Map<String, SendBatchResultData> result) {
                future.setResponse(result);
            }

            @Override
            public void onException(Map<String, List<ProduceMessage>> messages, Throwable cause) {
                future.setThrowable(cause);
            }
        });
        return future;
    }

    @Override
    public SendPrepareResult prepare(BrokerNode brokerNode, String topic, String app, String transactionId, long sequence, long transactionTimeout, long timeout) {
        checkState();
        ProducerClient client = producerClientManager.getOrCreateClient(brokerNode);
        handleAddProducers(brokerNode, Lists.newArrayList(topic), app, client);

        ProduceMessagePrepareAck produceMessagePrepareAck = client.produceMessagePrepare(topic, app, sequence, transactionId, transactionTimeout, timeout);
        return new SendPrepareResult(produceMessagePrepareAck.getTxId(), produceMessagePrepareAck.getCode());
    }

    @Override
    public JMQCode commit(BrokerNode brokerNode, String topic, String app, String txId, long timeout) {
        checkState();
        ProducerClient client = producerClientManager.getOrCreateClient(brokerNode);
        handleAddProducers(brokerNode, Lists.newArrayList(topic), app, client);

        ProduceMessageCommitAck produceMessageCommitAck = client.produceMessageCommit(topic, app, txId, timeout);
        return produceMessageCommitAck.getCode();
    }

    @Override
    public JMQCode rollback(BrokerNode brokerNode, String topic, String app, String txId, long timeout) {
        checkState();
        ProducerClient client = producerClientManager.getOrCreateClient(brokerNode);
        handleAddProducers(brokerNode, Lists.newArrayList(topic), app, client);

        ProduceMessageRollbackAck produceMessageRollbackAck = client.produceMessageRollback(topic, app, txId, timeout);
        return produceMessageRollbackAck.getCode();
    }

    @Override
    public FetchFeedbackData fetchFeedback(BrokerNode brokerNode, String topic, String app, TxStatus txStatus, int count, long longPollTimeout, long timeout) {
        checkState();
        ProducerClient client = producerClientManager.getOrCreateClient(brokerNode);
        handleAddProducers(brokerNode, Lists.newArrayList(topic), app, client);

        FetchProduceFeedbackAck fetchProduceFeedbackAck = client.fetchFeedback(topic, app, txStatus, count, longPollTimeout, timeout);
        return MessageSenderConverter.convertToFetchFeedbackData(topic, app, fetchProduceFeedbackAck);
    }

    protected void checkState() {
        if (!isStarted()) {
            throw new ClientException("sender is not started", JMQCode.CN_SERVICE_NOT_AVAILABLE.getCode());
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