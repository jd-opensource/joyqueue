package io.chubao.joyqueue.client.internal.producer;

import io.chubao.joyqueue.client.internal.producer.callback.AsyncBatchSendCallback;
import io.chubao.joyqueue.client.internal.producer.callback.AsyncMultiBatchSendCallback;
import io.chubao.joyqueue.client.internal.producer.callback.AsyncSendCallback;
import io.chubao.joyqueue.client.internal.producer.domain.FetchFeedbackData;
import io.chubao.joyqueue.client.internal.producer.domain.ProduceMessage;
import io.chubao.joyqueue.client.internal.producer.domain.SendBatchResultData;
import io.chubao.joyqueue.client.internal.producer.domain.SendPrepareResult;
import io.chubao.joyqueue.client.internal.producer.domain.SendResultData;
import io.chubao.joyqueue.client.internal.producer.transport.ProducerClientManager;
import io.chubao.joyqueue.domain.QosLevel;
import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.network.command.TxStatus;
import io.chubao.joyqueue.network.domain.BrokerNode;
import io.chubao.joyqueue.toolkit.service.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * MessageSenderWrapper
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/27
 */
public class MessageSenderWrapper extends Service implements MessageSender {

    private ProducerClientManager producerClientManager;
    private MessageSender delegate;

    public MessageSenderWrapper(ProducerClientManager producerClientManager, MessageSender delegate) {
        this.producerClientManager = producerClientManager;
        this.delegate = delegate;
    }

    @Override
    protected void doStart() throws Exception {
        producerClientManager.start();
        delegate.start();
    }

    @Override
    protected void doStop() {
        delegate.stop();
        producerClientManager.stop();
    }

    @Override
    public SendResultData send(BrokerNode brokerNode, String topic, String app, String txId, ProduceMessage message, QosLevel qosLevel, long produceTimeout, long timeout) {
        return delegate.send(brokerNode, topic, app, txId, message, qosLevel, produceTimeout, timeout);
    }

    @Override
    public SendBatchResultData batchSend(BrokerNode brokerNode, String topic, String app, String txId, List<ProduceMessage> messages, QosLevel qosLevel, long produceTimeout, long timeout) {
        return delegate.batchSend(brokerNode, topic, app, txId, messages, qosLevel, produceTimeout, timeout);
    }

    @Override
    public CompletableFuture<SendBatchResultData> batchSendAsync(BrokerNode brokerNode, String topic,
                                                                 String app, String txId,
                                                                 List<ProduceMessage> messages, QosLevel qosLevel,
                                                                 long produceTimeout, long timeout) {
        return delegate.batchSendAsync(brokerNode, topic, app, txId, messages, qosLevel, produceTimeout, timeout);
    }

    @Override
    public void sendAsync(BrokerNode brokerNode, String topic, String app, String txId, ProduceMessage message, QosLevel qosLevel, long produceTimeout, long timeout, AsyncSendCallback callback) {
        delegate.sendAsync(brokerNode, topic, app, txId, message, qosLevel, produceTimeout, timeout, callback);
    }

    @Override
    public void sendOneway(BrokerNode brokerNode, String topic, String app, String txId, ProduceMessage message, QosLevel qosLevel, long produceTimeout, long timeout) {
        delegate.sendOneway(brokerNode, topic, app, txId, message, qosLevel, produceTimeout, timeout);
    }

    @Override
    public void batchSendOneway(BrokerNode brokerNode, String app, String txId, Map<String, List<ProduceMessage>> messages, QosLevel qosLevel, long produceTimeout, long timeout) {
        delegate.batchSendOneway(brokerNode, app, txId, messages, qosLevel, produceTimeout, timeout);
    }

    @Override
    public void batchSendOneway(BrokerNode brokerNode, String topic,
                                String app, String txId,
                                List<ProduceMessage> messages, QosLevel qosLevel,
                                long produceTimeout, long timeout) {
        delegate.batchSendOneway(brokerNode, topic, app, txId, messages, qosLevel, produceTimeout, timeout);
    }

    @Override
    public void batchSendAsync(BrokerNode brokerNode, String topic,
                               String app, String txId,
                               List<ProduceMessage> messages, QosLevel qosLevel,
                               long produceTimeout, long timeout,
                               AsyncBatchSendCallback callback) {
        delegate.batchSendAsync(brokerNode, topic, app, txId, messages, qosLevel, produceTimeout, timeout, callback);
    }

    @Override
    public Map<String, SendBatchResultData> batchSend(BrokerNode brokerNode, String app,
                                                      String txId, Map<String, List<ProduceMessage>> messages,
                                                      QosLevel qosLevel, long produceTimeout,
                                                      long timeout) {
        return delegate.batchSend(brokerNode, app, txId, messages, qosLevel, produceTimeout, timeout);
    }

    @Override
    public void batchSendAsync(BrokerNode brokerNode, String app,
                               String txId, Map<String, List<ProduceMessage>> messages,
                               QosLevel qosLevel, long produceTimeout,
                               long timeout, AsyncMultiBatchSendCallback callback) {
        delegate.batchSendAsync(brokerNode, app, txId, messages, qosLevel, produceTimeout, timeout, callback);
    }

    @Override
    public CompletableFuture<Map<String, SendBatchResultData>> batchSendAsync(BrokerNode brokerNode, String app,
                                                                              String txId, Map<String, List<ProduceMessage>> messages,
                                                                              QosLevel qosLevel, long produceTimeout,
                                                                              long timeout) {
        return delegate.batchSendAsync(brokerNode, app, txId, messages, qosLevel, produceTimeout, timeout);
    }

    @Override
    public SendPrepareResult prepare(BrokerNode brokerNode, String topic, String app, String transactionId, long sequence, long transactionTimeout, long timeout) {
        return delegate.prepare(brokerNode, topic, app, transactionId, sequence, transactionTimeout, timeout);
    }

    @Override
    public JoyQueueCode commit(BrokerNode brokerNode, String topic, String app, String txId, long timeout) {
        return delegate.commit(brokerNode, topic, app, txId, timeout);
    }

    @Override
    public JoyQueueCode rollback(BrokerNode brokerNode, String topic, String app, String txId, long timeout) {
        return delegate.rollback(brokerNode, topic, app, txId, timeout);
    }

    @Override
    public FetchFeedbackData fetchFeedback(BrokerNode brokerNode, String topic, String app, TxStatus txStatus, int count, long longPollTimeout, long timeout) {
        return delegate.fetchFeedback(brokerNode, topic, app, txStatus, count, longPollTimeout, timeout);
    }
}