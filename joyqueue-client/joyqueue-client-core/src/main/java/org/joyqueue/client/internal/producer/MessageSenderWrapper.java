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
package org.joyqueue.client.internal.producer;

import org.joyqueue.client.internal.producer.callback.AsyncBatchSendCallback;
import org.joyqueue.client.internal.producer.callback.AsyncMultiBatchSendCallback;
import org.joyqueue.client.internal.producer.callback.AsyncSendCallback;
import org.joyqueue.client.internal.producer.domain.FetchFeedbackData;
import org.joyqueue.client.internal.producer.domain.ProduceMessage;
import org.joyqueue.client.internal.producer.domain.SendBatchResultData;
import org.joyqueue.client.internal.producer.domain.SendPrepareResult;
import org.joyqueue.client.internal.producer.domain.SendResultData;
import org.joyqueue.client.internal.producer.transport.ProducerClientManager;
import org.joyqueue.domain.QosLevel;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.network.command.TxStatus;
import org.joyqueue.network.domain.BrokerNode;
import org.joyqueue.toolkit.service.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * MessageSenderWrapper
 *
 * author: gaohaoxiang
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