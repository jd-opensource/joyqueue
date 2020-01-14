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

import org.joyqueue.client.internal.cluster.ClusterManager;
import org.joyqueue.client.internal.metadata.domain.TopicMetadata;
import org.joyqueue.client.internal.producer.MessageProducer;
import org.joyqueue.client.internal.producer.TransactionMessageProducer;
import org.joyqueue.client.internal.producer.domain.ProduceMessage;
import org.joyqueue.client.internal.producer.domain.SendResult;
import org.joyqueue.client.internal.producer.interceptor.ProducerInterceptor;
import org.joyqueue.client.internal.producer.transport.ProducerClientManager;
import org.joyqueue.toolkit.service.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * MessageProducerWrapper
 *
 * author: gaohaoxiang
 * date: 2018/12/27
 */
public class MessageProducerWrapper extends Service implements MessageProducer {

    private ClusterManager clusterManager;
    private ProducerClientManager producerClientManager;
    private MessageProducer delegate;

    public MessageProducerWrapper(ClusterManager clusterManager, ProducerClientManager producerClientManager, MessageProducer delegate) {
        this.clusterManager = clusterManager;
        this.producerClientManager = producerClientManager;
        this.delegate = delegate;
    }

    @Override
    protected void doStart() throws Exception {
        if (clusterManager != null) {
            clusterManager.start();
        }
        if (producerClientManager != null) {
            producerClientManager.start();
        }
        delegate.start();
    }

    @Override
    protected void doStop() {
        delegate.stop();
        if (producerClientManager != null) {
            producerClientManager.stop();
        }
        if (clusterManager != null) {
            clusterManager.stop();
        }
    }

    @Override
    public SendResult send(ProduceMessage message) {
        return delegate.send(message);
    }

    @Override
    public SendResult send(ProduceMessage message, long timeout, TimeUnit timeoutUnit) {
        return delegate.send(message, timeout, timeoutUnit);
    }

    @Override
    public List<SendResult> batchSend(List<ProduceMessage> messages) {
        return delegate.batchSend(messages);
    }

    @Override
    public List<SendResult> batchSend(List<ProduceMessage> messages, long timeout, TimeUnit timeoutUnit) {
        return delegate.batchSend(messages, timeout, timeoutUnit);
    }

    @Override
    public void sendOneway(ProduceMessage message) {
        delegate.sendOneway(message);
    }

    @Override
    public void sendOneway(ProduceMessage message, long timeout, TimeUnit timeoutUnit) {
        delegate.sendOneway(message, timeout, timeoutUnit);
    }

    @Override
    public void batchSendOneway(List<ProduceMessage> messages) {
        delegate.batchSendOneway(messages);
    }

    @Override
    public void batchSendOneway(List<ProduceMessage> messages, long timeout, TimeUnit timeoutUnit) {
        delegate.batchSendOneway(messages, timeout, timeoutUnit);
    }

    @Override
    public CompletableFuture<SendResult> sendAsync(ProduceMessage message) {
        return delegate.sendAsync(message);
    }

    @Override
    public CompletableFuture<SendResult> sendAsync(ProduceMessage message, long timeout, TimeUnit timeoutUnit) {
        return delegate.sendAsync(message, timeout, timeoutUnit);
    }

    @Override
    public CompletableFuture<List<SendResult>> batchSendAsync(List<ProduceMessage> messages) {
        return delegate.batchSendAsync(messages);
    }

    @Override
    public CompletableFuture<List<SendResult>> batchSendAsync(List<ProduceMessage> messages, long timeout, TimeUnit timeoutUnit) {
        return delegate.batchSendAsync(messages, timeout, timeoutUnit);
    }

    @Override
    public TransactionMessageProducer beginTransaction() {
        return delegate.beginTransaction();
    }

    @Override
    public TransactionMessageProducer beginTransaction(long timeout, TimeUnit timeoutUnit) {
        return delegate.beginTransaction(timeout, timeoutUnit);
    }

    @Override
    public TransactionMessageProducer beginTransaction(String transactionId) {
        return delegate.beginTransaction(transactionId);
    }

    @Override
    public TransactionMessageProducer beginTransaction(String transactionId, long timeout, TimeUnit timeoutUnit) {
        return delegate.beginTransaction(transactionId, timeout, timeoutUnit);
    }

    @Override
    public TopicMetadata getTopicMetadata(String topic) {
        return delegate.getTopicMetadata(topic);
    }

    @Override
    public void addInterceptor(ProducerInterceptor interceptor) {
        delegate.addInterceptor(interceptor);
    }

    @Override
    public void removeInterceptor(ProducerInterceptor interceptor) {
        delegate.removeInterceptor(interceptor);
    }
}