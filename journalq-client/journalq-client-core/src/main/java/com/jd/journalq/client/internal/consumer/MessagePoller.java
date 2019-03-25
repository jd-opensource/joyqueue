package com.jd.journalq.client.internal.consumer;

import com.jd.journalq.client.internal.consumer.callback.ConsumerListener;
import com.jd.journalq.client.internal.consumer.domain.ConsumeMessage;
import com.jd.journalq.client.internal.consumer.domain.ConsumeReply;
import com.jd.journalq.client.internal.metadata.domain.TopicMetadata;
import com.jd.journalq.common.exception.JMQCode;
import com.jd.journalq.toolkit.lang.LifeCycle;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * MessagePoller
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/11
 */
public interface MessagePoller extends LifeCycle {

    // poll
    ConsumeMessage pollOnce(String topic);

    ConsumeMessage pollOnce(String topic, long timeout, TimeUnit timeoutUnit);

    List<ConsumeMessage> poll(String topic);

    List<ConsumeMessage> poll(String topic, long timeout, TimeUnit timeoutUnit);

    void pollAsync(String topic, ConsumerListener listener);

    void pollAsync(String topic, long timeout, TimeUnit timeoutUnit, ConsumerListener listener);

    // poll partition
    ConsumeMessage pollPartitionOnce(String topic, short partition);

    ConsumeMessage pollPartitionOnce(String topic, short partition, long timeout, TimeUnit timeoutUnit);

    ConsumeMessage pollPartitionOnce(String topic, short partition, long index);

    ConsumeMessage pollPartitionOnce(String topic, short partition, long index, long timeout, TimeUnit timeoutUnit);

    List<ConsumeMessage> pollPartition(String topic, short partition);

    List<ConsumeMessage> pollPartition(String topic, short partition, long timeout, TimeUnit timeoutUnit);

    List<ConsumeMessage> pollPartition(String topic, short partition, long index);

    List<ConsumeMessage> pollPartition(String topic, short partition, long index, long timeout, TimeUnit timeoutUnit);

    void pollPartitionAsync(String topic, short partition, ConsumerListener listener);

    void pollPartitionAsync(String topic, short partition, long timeout, TimeUnit timeoutUnit, ConsumerListener listener);

    void pollPartitionAsync(String topic, short partition, long index, ConsumerListener listener);

    void pollPartitionAsync(String topic, short partition, long index, long timeout, TimeUnit timeoutUnit, ConsumerListener listener);

    // reply
    JMQCode reply(String topic, List<ConsumeReply> replyList);

    JMQCode replyOnce(String topic, ConsumeReply reply);

    // metadata
    TopicMetadata getTopicMetadata(String topic);
}