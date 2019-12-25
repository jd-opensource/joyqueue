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

import org.joyqueue.client.internal.metadata.domain.TopicMetadata;
import org.joyqueue.client.internal.producer.domain.ProduceMessage;
import org.joyqueue.client.internal.producer.domain.SendResult;
import org.joyqueue.client.internal.producer.interceptor.ProducerInterceptor;
import org.joyqueue.toolkit.lang.LifeCycle;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * MessageProducer
 *
 * author: gaohaoxiang
 * date: 2018/12/10
 */
public interface MessageProducer extends LifeCycle {

    SendResult send(ProduceMessage message);

    SendResult send(ProduceMessage message, long timeout, TimeUnit timeoutUnit);

    List<SendResult> batchSend(List<ProduceMessage> messages);

    List<SendResult> batchSend(List<ProduceMessage> messages, long timeout, TimeUnit timeoutUnit);

    // oneway
    void sendOneway(ProduceMessage message);

    void sendOneway(ProduceMessage message, long timeout, TimeUnit timeoutUnit);

    void batchSendOneway(List<ProduceMessage> messages);

    void batchSendOneway(List<ProduceMessage> messages, long timeout, TimeUnit timeoutUnit);

    // async
    CompletableFuture<SendResult> sendAsync(ProduceMessage message);

    CompletableFuture<SendResult> sendAsync(ProduceMessage message, long timeout, TimeUnit timeoutUnit);

    CompletableFuture<List<SendResult>> batchSendAsync(List<ProduceMessage> messages);

    CompletableFuture<List<SendResult>> batchSendAsync(List<ProduceMessage> messages, long timeout, TimeUnit timeoutUnit);

    // transaction
    TransactionMessageProducer beginTransaction();

    TransactionMessageProducer beginTransaction(long timeout, TimeUnit timeoutUnit);

    TransactionMessageProducer beginTransaction(String transactionId);

    TransactionMessageProducer beginTransaction(String transactionId, long timeout, TimeUnit timeoutUnit);

    // metadata
    TopicMetadata getTopicMetadata(String topic);

    // interceptor
    void addInterceptor(ProducerInterceptor interceptor);

    void removeInterceptor(ProducerInterceptor interceptor);
}