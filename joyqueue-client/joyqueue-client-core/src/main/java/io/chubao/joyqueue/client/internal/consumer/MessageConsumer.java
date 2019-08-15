package io.chubao.joyqueue.client.internal.consumer;

import io.chubao.joyqueue.client.internal.consumer.domain.ConsumeMessage;
import io.chubao.joyqueue.client.internal.consumer.domain.ConsumeReply;
import io.chubao.joyqueue.client.internal.consumer.interceptor.ConsumerInterceptor;
import io.chubao.joyqueue.client.internal.metadata.domain.TopicMetadata;
import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.toolkit.lang.LifeCycle;

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

    // metadata
    TopicMetadata getTopicMetadata(String topic);
}