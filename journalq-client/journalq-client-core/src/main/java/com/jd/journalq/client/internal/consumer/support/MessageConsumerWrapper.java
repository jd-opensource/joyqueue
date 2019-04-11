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
package com.jd.journalq.client.internal.consumer.support;

import com.jd.journalq.client.internal.cluster.ClusterClientManager;
import com.jd.journalq.client.internal.cluster.ClusterManager;
import com.jd.journalq.client.internal.consumer.BatchMessageListener;
import com.jd.journalq.client.internal.consumer.MessageConsumer;
import com.jd.journalq.client.internal.consumer.MessageListener;
import com.jd.journalq.client.internal.consumer.callback.ConsumerListener;
import com.jd.journalq.client.internal.consumer.config.ConsumerConfig;
import com.jd.journalq.client.internal.consumer.domain.ConsumeMessage;
import com.jd.journalq.client.internal.consumer.domain.ConsumeReply;
import com.jd.journalq.client.internal.consumer.interceptor.ConsumerInterceptor;
import com.jd.journalq.client.internal.consumer.transport.ConsumerClientManager;
import com.jd.journalq.client.internal.metadata.domain.TopicMetadata;
import com.jd.journalq.client.internal.nameserver.NameServerConfig;
import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.toolkit.service.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * MessageConsumerWrapper
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/1/10
 */
public class MessageConsumerWrapper extends Service implements MessageConsumer {

    private ConsumerConfig consumerConfig;
    private NameServerConfig nameServerConfig;
    private ClusterManager clusterManager;
    private ClusterClientManager clusterClientManager;
    private ConsumerClientManager consumerClientManager;
    private MessageConsumer delegate;

    public MessageConsumerWrapper(ConsumerConfig consumerConfig, NameServerConfig nameServerConfig, ClusterManager clusterManager, ClusterClientManager clusterClientManager, ConsumerClientManager consumerClientManager, MessageConsumer delegate) {
        this.consumerConfig = consumerConfig;
        this.nameServerConfig = nameServerConfig;
        this.clusterManager = clusterManager;
        this.clusterClientManager = clusterClientManager;
        this.consumerClientManager = consumerClientManager;
        this.delegate = delegate;
    }

    @Override
    protected void doStart() throws Exception {
        if (clusterClientManager != null) {
            clusterClientManager.start();
        }
        if (clusterManager != null) {
            clusterManager.start();
        }
        if (consumerClientManager != null) {
            consumerClientManager.start();
        }
        delegate.start();
    }

    @Override
    protected void doStop() {
        delegate.stop();
        if (consumerClientManager != null) {
            consumerClientManager.stop();
        }
        if (clusterManager != null) {
            clusterManager.stop();
        }
        if (clusterClientManager != null) {
            clusterClientManager.stop();
        }
    }

    @Override
    public void subscribe(String topic) {
        delegate.subscribe(topic);
    }

    @Override
    public void unsubscribe() {
        delegate.unsubscribe();
    }

    @Override
    public String subscription() {
        return delegate.subscription();
    }

    @Override
    public boolean isSubscribed() {
        return delegate.isSubscribed();
    }

    @Override
    public void subscribe(String topic, MessageListener messageListener) {
        delegate.subscribe(topic, messageListener);
    }

    @Override
    public void subscribeBatch(String topic, BatchMessageListener batchMessageListener) {
        delegate.subscribeBatch(topic, batchMessageListener);
    }

    @Override
    public void resumeListen() {
        delegate.resumeListen();
    }

    @Override
    public void suspendListen() {
        delegate.suspendListen();
    }

    @Override
    public boolean isListenSuspended() {
        return delegate.isListenSuspended();
    }

    @Override
    public void addInterceptor(ConsumerInterceptor interceptor) {
        delegate.addInterceptor(interceptor);
    }

    @Override
    public void removeInterceptor(ConsumerInterceptor interceptor) {
        delegate.removeInterceptor(interceptor);
    }

    @Override
    public ConsumeMessage pollOnce() {
        return delegate.pollOnce();
    }

    @Override
    public ConsumeMessage pollOnce(long timeout, TimeUnit timeoutUnit) {
        return delegate.pollOnce(timeout, timeoutUnit);
    }

    @Override
    public List<ConsumeMessage> poll() {
        return delegate.poll();
    }

    @Override
    public List<ConsumeMessage> poll(long timeout, TimeUnit timeoutUnit) {
        return delegate.poll(timeout, timeoutUnit);
    }

    @Override
    public void pollAsync(ConsumerListener listener) {
        delegate.pollAsync(listener);
    }

    @Override
    public void pollAsync(long timeout, TimeUnit timeoutUnit, ConsumerListener listener) {
        delegate.pollAsync(timeout, timeoutUnit, listener);
    }

    @Override
    public ConsumeMessage pollPartitionOnce(short partition) {
        return delegate.pollPartitionOnce(partition);
    }

    @Override
    public ConsumeMessage pollPartitionOnce(short partition, long timeout, TimeUnit timeoutUnit) {
        return delegate.pollPartitionOnce(partition, timeout, timeoutUnit);
    }

    @Override
    public ConsumeMessage pollPartitionOnce(short partition, long index) {
        return delegate.pollPartitionOnce(partition, index);
    }

    @Override
    public ConsumeMessage pollPartitionOnce(short partition, long index, long timeout, TimeUnit timeoutUnit) {
        return delegate.pollPartitionOnce(partition, index, timeout, timeoutUnit);
    }

    @Override
    public List<ConsumeMessage> pollPartition(short partition) {
        return delegate.pollPartition(partition);
    }

    @Override
    public List<ConsumeMessage> pollPartition(short partition, long timeout, TimeUnit timeoutUnit) {
        return delegate.pollPartition(partition, timeout, timeoutUnit);
    }

    @Override
    public List<ConsumeMessage> pollPartition(short partition, long index) {
        return delegate.pollPartition(partition, index);
    }

    @Override
    public List<ConsumeMessage> pollPartition(short partition, long index, long timeout, TimeUnit timeoutUnit) {
        return delegate.pollPartition(partition, index, timeout, timeoutUnit);
    }

    @Override
    public void pollPartitionAsync(short partition, ConsumerListener listener) {
        delegate.pollPartitionAsync(partition, listener);
    }

    @Override
    public void pollPartitionAsync(short partition, long timeout, TimeUnit timeoutUnit, ConsumerListener listener) {
        delegate.pollPartitionAsync(partition, timeout, timeoutUnit, listener);
    }

    @Override
    public void pollPartitionAsync(short partition, long index, ConsumerListener listener) {
        delegate.pollPartitionAsync(partition, index, listener);
    }

    @Override
    public void pollPartitionAsync(short partition, long index, long timeout, TimeUnit timeoutUnit, ConsumerListener listener) {
        delegate.pollPartitionAsync(partition, index, timeout, timeoutUnit, listener);
    }

    @Override
    public JMQCode reply(List<ConsumeReply> replyList) {
        return delegate.reply(replyList);
    }

    @Override
    public JMQCode replyOnce(ConsumeReply reply) {
        return delegate.replyOnce(reply);
    }

    @Override
    public TopicMetadata getTopicMetadata(String topic) {
        return delegate.getTopicMetadata(topic);
    }
}