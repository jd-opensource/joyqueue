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
import org.joyqueue.client.internal.cluster.ClusterManager;
import org.joyqueue.client.internal.metadata.domain.TopicMetadata;
import org.joyqueue.client.internal.nameserver.NameServerConfig;
import org.joyqueue.client.internal.producer.MessageProducer;
import org.joyqueue.client.internal.producer.MessageSender;
import org.joyqueue.client.internal.producer.TransactionMessageProducer;
import org.joyqueue.client.internal.producer.callback.AsyncBatchProduceCallback;
import org.joyqueue.client.internal.producer.callback.AsyncProduceCallback;
import org.joyqueue.client.internal.producer.callback.CompletableFutureAsyncBatchProduceCallback;
import org.joyqueue.client.internal.producer.callback.CompletableFutureAsyncProduceCallback;
import org.joyqueue.client.internal.producer.config.ProducerConfig;
import org.joyqueue.client.internal.producer.config.SenderConfig;
import org.joyqueue.client.internal.producer.domain.ProduceMessage;
import org.joyqueue.client.internal.producer.domain.SendResult;
import org.joyqueue.client.internal.producer.exception.ProducerException;
import org.joyqueue.client.internal.producer.interceptor.ProducerInterceptor;
import org.joyqueue.client.internal.producer.interceptor.ProducerInterceptorManager;
import org.joyqueue.client.internal.producer.transport.ProducerClientManager;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.toolkit.service.Service;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * DefaultMessageProducer
 *
 * author: gaohaoxiang
 * date: 2018/12/10
 */
public class DefaultMessageProducer extends Service implements MessageProducer {

    protected static final Logger logger = LoggerFactory.getLogger(DefaultMessageProducer.class);

    private ProducerConfig config;
    private NameServerConfig nameServerConfig;
    private ClusterManager clusterManager;
    private ProducerClientManager producerClientManager;

    private SenderConfig senderConfig;
    private MessageSender messageSender;
    private AtomicLong transactionSequence;
    private MessageProducerInner messageProducerInner;
    private ProducerInterceptorManager producerInterceptorManager = new ProducerInterceptorManager();

    public DefaultMessageProducer(ProducerConfig config, NameServerConfig nameServerConfig, ClusterManager clusterManager, ProducerClientManager producerClientManager) {
        Preconditions.checkArgument(config != null, "producer not null");
        Preconditions.checkArgument(nameServerConfig != null, "nameServer not null");
        Preconditions.checkArgument(clusterManager != null, "clusterManager not null");
        Preconditions.checkArgument(producerClientManager != null, "producerClientManager not null");
        Preconditions.checkArgument(StringUtils.isNotBlank(config.getApp()), "producer.app not blank");
        Preconditions.checkArgument(config.getRetryPolicy() != null, "producer.retryPolicy not null");
        Preconditions.checkArgument(config.getQosLevel() != null, "producer.qosLevel not null");

        this.config = config;
        this.nameServerConfig = nameServerConfig;
        this.clusterManager = clusterManager;
        this.producerClientManager = producerClientManager;
    }

    @Override
    protected void validate() throws Exception {
        transactionSequence = new AtomicLong();
        senderConfig = new SenderConfig(config.isCompress(), config.getCompressThreshold(), config.getCompressType(), config.isBatch());
        messageSender = new DefaultMessageSender(producerClientManager, senderConfig);
        messageProducerInner = new MessageProducerInner(config, nameServerConfig, messageSender, clusterManager, producerClientManager, producerInterceptorManager);
    }

    @Override
    protected void doStart() throws Exception {
        messageSender.start();
        messageProducerInner.start();
    }

    @Override
    protected void doStop() {
        if (messageProducerInner != null) {
            messageProducerInner.stop();
        }
        if (messageSender != null) {
            messageSender.stop();
        }
    }

    @Override
    public SendResult send(ProduceMessage message) {
        return send(message, config.getTimeout(), TimeUnit.MILLISECONDS);
    }

    @Override
    public SendResult send(ProduceMessage message, long timeout, TimeUnit timeoutUnit) {
        return doSend(message, timeout, timeoutUnit, false, null);
    }

    @Override
    public List<SendResult> batchSend(List<ProduceMessage> messages) {
        return batchSend(messages, config.getTimeout(), TimeUnit.MILLISECONDS);
    }

    @Override
    public List<SendResult> batchSend(List<ProduceMessage> messages, long timeout, TimeUnit timeoutUnit) {
        return doBatchSend(messages, timeout, timeoutUnit, false, null);
    }

    @Override
    public void sendOneway(ProduceMessage message) {
        sendOneway(message, config.getTimeout(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void sendOneway(ProduceMessage message, long timeout, TimeUnit timeoutUnit) {
        doSend(message, timeout, timeoutUnit, true, null);
    }

    @Override
    public void batchSendOneway(List<ProduceMessage> messages) {
        batchSendOneway(messages, config.getTimeout(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void batchSendOneway(List<ProduceMessage> messages, long timeout, TimeUnit timeoutUnit) {
        doBatchSend(messages, timeout, timeoutUnit, true, null);
    }

    @Override
    public CompletableFuture<SendResult> sendAsync(ProduceMessage message) {
        return sendAsync(message, config.getTimeout(), TimeUnit.MILLISECONDS);
    }

    @Override
    public CompletableFuture<SendResult> sendAsync(ProduceMessage message, long timeout, TimeUnit timeoutUnit) {
        CompletableFuture<SendResult> future = new CompletableFuture();
        doSend(message, timeout, timeoutUnit, false, new CompletableFutureAsyncProduceCallback(future));
        return future;
    }

    @Override
    public CompletableFuture<List<SendResult>> batchSendAsync(List<ProduceMessage> messages) {
        return batchSendAsync(messages, config.getTimeout(), TimeUnit.MILLISECONDS);
    }

    @Override
    public CompletableFuture<List<SendResult>> batchSendAsync(List<ProduceMessage> messages, long timeout, TimeUnit timeoutUnit) {
        CompletableFuture<List<SendResult>> future = new CompletableFuture();
        doBatchSend(messages, timeout, timeoutUnit, false, new CompletableFutureAsyncBatchProduceCallback(future));
        return future;
    }

    protected SendResult doSend(ProduceMessage message, long timeout, TimeUnit timeoutUnit, boolean isOneway, AsyncProduceCallback callback) {
        checkState();
        return messageProducerInner.send(message, null, timeout, timeoutUnit, isOneway, config.isFailover(), callback);
    }

    protected List<SendResult> doBatchSend(List<ProduceMessage> messages, long timeout, TimeUnit timeoutUnit, boolean isOneway, AsyncBatchProduceCallback callback) {
        checkState();
        return messageProducerInner.batchSend(messages, null, timeout, timeoutUnit, isOneway, config.isFailover(), callback);
    }

    @Override
    public TransactionMessageProducer beginTransaction() {
        return beginTransaction(config.getTransactionTimeout(), TimeUnit.MILLISECONDS);
    }

    @Override
    public TransactionMessageProducer beginTransaction(long timeout, TimeUnit timeoutUnit) {
        return new DefaultTransactionMessageProducer(null, timeout, timeoutUnit, transactionSequence.getAndIncrement(), config, nameServerConfig, clusterManager, messageSender, messageProducerInner);
    }

    @Override
    public TransactionMessageProducer beginTransaction(String transactionId) {
        return beginTransaction(transactionId, config.getTransactionTimeout(), TimeUnit.MILLISECONDS);
    }

    @Override
    public TransactionMessageProducer beginTransaction(String transactionId, long timeout, TimeUnit timeoutUnit) {
        checkState();
        Preconditions.checkArgument(StringUtils.isNotBlank(transactionId), "transactionId not blank");
        return new DefaultTransactionMessageProducer(transactionId, timeout,
                timeoutUnit, transactionSequence.getAndIncrement(),
                config, nameServerConfig,
                clusterManager, messageSender,
                messageProducerInner);
    }

    @Override
    public TopicMetadata getTopicMetadata(String topic) {
        checkState();
        Preconditions.checkArgument(StringUtils.isNotBlank(topic), "topic not blank");

        String topicFullName = messageProducerInner.getTopicFullName(topic);
        return clusterManager.fetchTopicMetadata(topicFullName, config.getApp());
    }

    @Override
    public synchronized void addInterceptor(ProducerInterceptor interceptor) {
        Preconditions.checkArgument(interceptor != null, "interceptor can not be null");

        producerInterceptorManager.addInterceptor(interceptor);
    }

    @Override
    public synchronized void removeInterceptor(ProducerInterceptor interceptor) {
        Preconditions.checkArgument(interceptor != null, "interceptor can not be null");

        producerInterceptorManager.removeInterceptor(interceptor);
    }

    protected void checkState() {
        if (!isStarted()) {
            throw new ProducerException("producer is not started", JoyQueueCode.CN_SERVICE_NOT_AVAILABLE.getCode());
        }
    }
}