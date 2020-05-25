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
package org.joyqueue.client.internal.consumer;

import org.joyqueue.client.internal.consumer.domain.ConsumeMessage;
import org.joyqueue.client.internal.consumer.domain.ConsumeReply;
import org.joyqueue.client.internal.consumer.domain.FetchIndexData;
import org.joyqueue.client.internal.consumer.interceptor.ConsumerInterceptor;
import org.joyqueue.client.internal.metadata.domain.TopicMetadata;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.toolkit.lang.LifeCycle;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * MessageConsumer
 *
 * author: gaohaoxiang
 * date: 2019/1/10
 */
public interface MessageConsumer extends LifeCycle {

    // subscribe
    void subscribe(String topic);

    void unsubscribe();

    String subscription();

    boolean isSubscribed();

    // listen
    void subscribe(String topic, MessageListener messageListener);

    void subscribeBatch(String topic, BatchMessageListener batchMessageListener);

    void resumeListen();

    void suspendListen();

    boolean isListenSuspended();

    // interceptor
    void addInterceptor(ConsumerInterceptor interceptor);

    void removeInterceptor(ConsumerInterceptor interceptor);

    // poll
    ConsumeMessage pollOnce();

    ConsumeMessage pollOnce(long timeout, TimeUnit timeoutUnit);

    List<ConsumeMessage> poll();

    List<ConsumeMessage> poll(long timeout, TimeUnit timeoutUnit);

    CompletableFuture<List<ConsumeMessage>> pollAsync();

    CompletableFuture<List<ConsumeMessage>> pollAsync(long timeout, TimeUnit timeoutUnit);

    // poll partition
    ConsumeMessage pollPartitionOnce(short partition);

    ConsumeMessage pollPartitionOnce(short partition, long timeout, TimeUnit timeoutUnit);

    ConsumeMessage pollPartitionOnce(short partition, long index);

    ConsumeMessage pollPartitionOnce(short partition, long index, long timeout, TimeUnit timeoutUnit);

    List<ConsumeMessage> pollPartition(short partition);

    List<ConsumeMessage> pollPartition(short partition, long timeout, TimeUnit timeoutUnit);

    List<ConsumeMessage> pollPartition(short partition, long index);

    List<ConsumeMessage> pollPartition(short partition, long index, long timeout, TimeUnit timeoutUnit);

    // async

    CompletableFuture<List<ConsumeMessage>> pollPartitionAsync(short partition);

    CompletableFuture<List<ConsumeMessage>> pollPartitionAsync(short partition, long timeout, TimeUnit timeoutUnit);

    CompletableFuture<List<ConsumeMessage>> pollPartitionAsync(short partition, long index);

    CompletableFuture<List<ConsumeMessage>> pollPartitionAsync(short partition, long index, long timeout, TimeUnit timeoutUnit);

    // reply
    JoyQueueCode reply(List<ConsumeReply> replyList);

    JoyQueueCode replyOnce(ConsumeReply reply);

    // commit
    JoyQueueCode commitIndex(short partition, long index);

    JoyQueueCode commitMaxIndex(short partition);

    JoyQueueCode commitMaxIndex();

    JoyQueueCode commitMinIndex(short partition);

    JoyQueueCode commitMinIndex();

    // index
    FetchIndexData fetchIndex(short partition);

    // metadata
    TopicMetadata getTopicMetadata(String topic);
}