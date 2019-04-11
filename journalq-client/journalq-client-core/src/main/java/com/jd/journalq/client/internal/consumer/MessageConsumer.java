/**
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
package com.jd.journalq.client.internal.consumer;

import com.jd.journalq.client.internal.consumer.callback.ConsumerListener;
import com.jd.journalq.client.internal.consumer.domain.ConsumeMessage;
import com.jd.journalq.client.internal.consumer.domain.ConsumeReply;
import com.jd.journalq.client.internal.consumer.interceptor.ConsumerInterceptor;
import com.jd.journalq.client.internal.metadata.domain.TopicMetadata;
import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.toolkit.lang.LifeCycle;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * MessageConsumer
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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

    void pollAsync(ConsumerListener listener);

    void pollAsync(long timeout, TimeUnit timeoutUnit, ConsumerListener listener);

    // poll partition
    ConsumeMessage pollPartitionOnce(short partition);

    ConsumeMessage pollPartitionOnce(short partition, long timeout, TimeUnit timeoutUnit);

    ConsumeMessage pollPartitionOnce(short partition, long index);

    ConsumeMessage pollPartitionOnce(short partition, long index, long timeout, TimeUnit timeoutUnit);

    List<ConsumeMessage> pollPartition(short partition);

    List<ConsumeMessage> pollPartition(short partition, long timeout, TimeUnit timeoutUnit);

    List<ConsumeMessage> pollPartition(short partition, long index);

    List<ConsumeMessage> pollPartition(short partition, long index, long timeout, TimeUnit timeoutUnit);

    void pollPartitionAsync(short partition, ConsumerListener listener);

    void pollPartitionAsync(short partition, long timeout, TimeUnit timeoutUnit, ConsumerListener listener);

    void pollPartitionAsync(short partition, long index, ConsumerListener listener);

    void pollPartitionAsync(short partition, long index, long timeout, TimeUnit timeoutUnit, ConsumerListener listener);

    // reply
    JMQCode reply(List<ConsumeReply> replyList);

    JMQCode replyOnce(ConsumeReply reply);

    // metadata
    TopicMetadata getTopicMetadata(String topic);
}