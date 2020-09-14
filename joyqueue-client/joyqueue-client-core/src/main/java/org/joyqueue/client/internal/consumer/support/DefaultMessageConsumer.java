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
package org.joyqueue.client.internal.consumer.support;

import com.google.common.base.Preconditions;
import org.joyqueue.client.internal.cluster.ClusterClientManager;
import org.joyqueue.client.internal.cluster.ClusterManager;
import org.joyqueue.client.internal.consumer.BaseMessageListener;
import org.joyqueue.client.internal.consumer.BatchMessageListener;
import org.joyqueue.client.internal.consumer.MessageConsumer;
import org.joyqueue.client.internal.consumer.MessageListener;
import org.joyqueue.client.internal.consumer.config.ConsumerConfig;
import org.joyqueue.client.internal.consumer.domain.ConsumeMessage;
import org.joyqueue.client.internal.consumer.domain.ConsumeReply;
import org.joyqueue.client.internal.consumer.domain.FetchIndexData;
import org.joyqueue.client.internal.consumer.exception.ConsumerException;
import org.joyqueue.client.internal.consumer.interceptor.ConsumerInterceptor;
import org.joyqueue.client.internal.consumer.interceptor.ConsumerInterceptorManager;
import org.joyqueue.client.internal.consumer.transport.ConsumerClientManager;
import org.joyqueue.client.internal.exception.ClientException;
import org.joyqueue.client.internal.metadata.domain.TopicMetadata;
import org.joyqueue.client.internal.nameserver.NameServerConfig;
import org.joyqueue.client.internal.nameserver.helper.NameServerHelper;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.toolkit.service.Service;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * DefaultMessageConsumer
 *
 * author: gaohaoxiang
 * date: 2019/1/10
 */
public class DefaultMessageConsumer extends Service implements MessageConsumer {

    protected static final Logger logger = LoggerFactory.getLogger(DefaultMessageConsumer.class);

    private ConsumerConfig config;
    private NameServerConfig nameServerConfig;
    private ClusterManager clusterManager;
    private ClusterClientManager clusterClientManager;
    private ConsumerClientManager consumerClientManager;

    private String subscribeTopic;
    private TopicMessageConsumer topicMessageConsumer;
    private ConsumerInterceptorManager consumerInterceptorManager = new ConsumerInterceptorManager();

    public DefaultMessageConsumer(ConsumerConfig config, NameServerConfig nameServerConfig, ClusterManager clusterManager,
                                  ClusterClientManager clusterClientManager, ConsumerClientManager consumerClientManager) {
        Preconditions.checkArgument(config != null, "config not null");
        Preconditions.checkArgument(nameServerConfig != null, "nameserver not null");
        Preconditions.checkArgument(clusterManager != null, "clusterManager not null");
        Preconditions.checkArgument(clusterClientManager != null, "clusterClientManager not null");
        Preconditions.checkArgument(consumerClientManager != null, "consumerClientManager not null");

        this.config = config;
        this.nameServerConfig = nameServerConfig;
        this.clusterManager = clusterManager;
        this.clusterClientManager = clusterClientManager;
        this.consumerClientManager = consumerClientManager;
    }

    @Override
    protected void doStart() throws Exception {
        if (topicMessageConsumer != null) {
            topicMessageConsumer.start();
        }
    }

    @Override
    protected void doStop() {
        if (topicMessageConsumer != null) {
            topicMessageConsumer.stop();
        }
    }

    @Override
    public synchronized void subscribe(String topic) {
        Preconditions.checkArgument(StringUtils.isNotBlank(topic), "topic can not be null");

        checkUnsubscribe();
        TopicMessageConsumer topicMessageConsumer = newTopicMessageConsumer(topic);
        this.topicMessageConsumer = topicMessageConsumer;
        this.subscribeTopic = topic;
    }

    @Override
    public void unsubscribe() {
        checkSubscribe();
        subscribeTopic = null;
        if (isStarted()) {
            topicMessageConsumer.stop();
        }
    }

    @Override
    public String subscription() {
        return subscribeTopic;
    }

    @Override
    public boolean isSubscribed() {
        return StringUtils.isNotBlank(subscribeTopic);
    }

    @Override
    public synchronized void subscribe(String topic, MessageListener messageListener) {
        doSubscribeListener(topic, messageListener);
    }

    @Override
    public synchronized void subscribeBatch(String topic, BatchMessageListener batchMessageListener) {
        doSubscribeListener(topic, batchMessageListener);
    }

    protected void doSubscribeListener(String topic, BaseMessageListener messageListener) {
        Preconditions.checkArgument(StringUtils.isNotBlank(topic), "topic can not be null");
        Preconditions.checkArgument(messageListener != null, "listener can not be null");

        checkUnsubscribe();
        TopicMessageConsumer topicMessageConsumer = newTopicMessageConsumer(topic);
        topicMessageConsumer.addListener(messageListener);
        this.topicMessageConsumer = topicMessageConsumer;
        this.subscribeTopic = topic;
    }

    protected TopicMessageConsumer newTopicMessageConsumer(String topic) {
        TopicMessageConsumer topicMessageConsumer = new TopicMessageConsumer(topic, config, nameServerConfig, clusterManager, clusterClientManager, consumerClientManager, consumerInterceptorManager);
        try {
            if (isStarted()) {
                topicMessageConsumer.start();
            }
        } catch (IllegalArgumentException e) {
            logger.debug("newTopicMessageConsumer exception, topic: {}", topic, e);
            throw e;
        } catch (ClientException e) {
            logger.debug("newTopicMessageConsumer exception, topic: {}", topic, e);
            throw new ConsumerException(e.getMessage(), e.getCode(), e);
        } catch (Exception e) {
            logger.debug("newTopicMessageConsumer exception, topic: {}", topic, e);
            throw new ConsumerException(JoyQueueCode.CN_UNKNOWN_ERROR.getMessage(), JoyQueueCode.CN_UNKNOWN_ERROR.getCode(), e);
        }
        return topicMessageConsumer;
    }

    @Override
    public void resumeListen() {
        checkState();
        checkSubscribe();
        topicMessageConsumer.resume();
    }

    @Override
    public void suspendListen() {
        checkState();
        checkSubscribe();
        topicMessageConsumer.suspend();
    }

    @Override
    public boolean isListenSuspended() {
        checkState();
        checkSubscribe();
        return topicMessageConsumer.isSuspend();
    }

    @Override
    public synchronized void addInterceptor(ConsumerInterceptor interceptor) {
        Preconditions.checkArgument(interceptor != null, "interceptor can not be null");

        consumerInterceptorManager.addInterceptor(interceptor);
    }

    @Override
    public synchronized void removeInterceptor(ConsumerInterceptor interceptor) {
        Preconditions.checkArgument(interceptor != null, "interceptor can not be null");

        consumerInterceptorManager.removeInterceptor(interceptor);
    }

    @Override
    public ConsumeMessage pollOnce() {
        checkState();
        checkSubscribe();
        return topicMessageConsumer.getMessagePoller().pollOnce(subscribeTopic);
    }

    @Override
    public ConsumeMessage pollOnce(long timeout, TimeUnit timeoutUnit) {
        checkState();
        checkSubscribe();
        return topicMessageConsumer.getMessagePoller().pollOnce(subscribeTopic, timeout, timeoutUnit);
    }

    @Override
    public List<ConsumeMessage> poll() {
        checkState();
        checkSubscribe();
        return topicMessageConsumer.getMessagePoller().poll(subscribeTopic);
    }

    @Override
    public List<ConsumeMessage> poll(long timeout, TimeUnit timeoutUnit) {
        checkState();
        checkSubscribe();
        return topicMessageConsumer.getMessagePoller().poll(subscribeTopic, timeout, timeoutUnit);
    }

    @Override
    public CompletableFuture<List<ConsumeMessage>> pollAsync() {
        checkState();
        checkSubscribe();
        return topicMessageConsumer.getMessagePoller().pollAsync(subscribeTopic);
    }

    @Override
    public CompletableFuture<List<ConsumeMessage>> pollAsync(long timeout, TimeUnit timeoutUnit) {
        checkState();
        checkSubscribe();
        return topicMessageConsumer.getMessagePoller().pollAsync(subscribeTopic, timeout, timeoutUnit);
    }

    @Override
    public ConsumeMessage pollPartitionOnce(short partition) {
        checkState();
        checkSubscribe();
        return topicMessageConsumer.getMessagePoller().pollPartitionOnce(subscribeTopic, partition);
    }

    @Override
    public ConsumeMessage pollPartitionOnce(short partition, long timeout, TimeUnit timeoutUnit) {
        checkState();
        checkSubscribe();
        return topicMessageConsumer.getMessagePoller().pollPartitionOnce(subscribeTopic, partition, timeout, timeoutUnit);
    }

    @Override
    public ConsumeMessage pollPartitionOnce(short partition, long index) {
        checkState();
        checkSubscribe();
        return topicMessageConsumer.getMessagePoller().pollPartitionOnce(subscribeTopic, partition, index);
    }

    @Override
    public ConsumeMessage pollPartitionOnce(short partition, long index, long timeout, TimeUnit timeoutUnit) {
        checkState();
        checkSubscribe();
        return topicMessageConsumer.getMessagePoller().pollPartitionOnce(subscribeTopic, partition, index, timeout, timeoutUnit);
    }

    @Override
    public List<ConsumeMessage> pollPartition(short partition) {
        checkState();
        checkSubscribe();
        return topicMessageConsumer.getMessagePoller().pollPartition(subscribeTopic, partition);
    }

    @Override
    public List<ConsumeMessage> pollPartition(short partition, long timeout, TimeUnit timeoutUnit) {
        checkState();
        checkSubscribe();
        return topicMessageConsumer.getMessagePoller().pollPartition(subscribeTopic, partition, timeout, timeoutUnit);
    }

    @Override
    public CompletableFuture<List<ConsumeMessage>> pollPartitionAsync(short partition) {
        checkState();
        checkSubscribe();
        return topicMessageConsumer.getMessagePoller().pollPartitionAsync(subscribeTopic, partition);
    }

    @Override
    public CompletableFuture<List<ConsumeMessage>> pollPartitionAsync(short partition, long timeout, TimeUnit timeoutUnit) {
        checkState();
        checkSubscribe();
        return topicMessageConsumer.getMessagePoller().pollPartitionAsync(subscribeTopic, partition, timeout, timeoutUnit);
    }

    @Override
    public List<ConsumeMessage> pollPartition(short partition, long index) {
        checkState();
        checkSubscribe();
        return topicMessageConsumer.getMessagePoller().pollPartition(subscribeTopic, partition, index);
    }

    @Override
    public List<ConsumeMessage> pollPartition(short partition, long index, long timeout, TimeUnit timeoutUnit) {
        checkState();
        checkSubscribe();
        return topicMessageConsumer.getMessagePoller().pollPartition(subscribeTopic, partition, index, timeout, timeoutUnit);
    }

    @Override
    public CompletableFuture<List<ConsumeMessage>> pollPartitionAsync(short partition, long index) {
        checkState();
        checkSubscribe();
        return topicMessageConsumer.getMessagePoller().pollPartitionAsync(subscribeTopic, partition, index);
    }

    @Override
    public CompletableFuture<List<ConsumeMessage>> pollPartitionAsync(short partition, long index, long timeout, TimeUnit timeoutUnit) {
        checkState();
        checkSubscribe();
        return topicMessageConsumer.getMessagePoller().pollPartitionAsync(subscribeTopic, partition, index, timeout, timeoutUnit);
    }

    @Override
    public JoyQueueCode reply(List<ConsumeReply> replyList) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(replyList), "replyList can not be null");

        checkState();
        checkSubscribe();
        return topicMessageConsumer.getMessagePoller().reply(subscribeTopic, replyList);
    }

    @Override
    public JoyQueueCode replyOnce(ConsumeReply reply) {
        Preconditions.checkArgument(reply != null, "replyList can not be null");

        checkState();
        checkSubscribe();
        return topicMessageConsumer.getMessagePoller().replyOnce(subscribeTopic, reply);
    }

    @Override
    public JoyQueueCode commitIndex(short partition, long index) {
        checkState();
        checkSubscribe();
        return topicMessageConsumer.getMessagePoller().commitIndex(subscribeTopic, partition, index);
    }

    @Override
    public JoyQueueCode commitMaxIndex() {
        checkState();
        checkSubscribe();
        return topicMessageConsumer.getMessagePoller().commitMaxIndex(subscribeTopic);
    }

    @Override
    public JoyQueueCode commitMaxIndex(short partition) {
        checkState();
        checkSubscribe();
        return topicMessageConsumer.getMessagePoller().commitMaxIndex(subscribeTopic, partition);
    }

    @Override
    public JoyQueueCode commitMinIndex() {
        checkState();
        checkSubscribe();
        return topicMessageConsumer.getMessagePoller().commitMinIndex(subscribeTopic);
    }

    @Override
    public JoyQueueCode commitMinIndex(short partition) {
        checkState();
        checkSubscribe();
        return topicMessageConsumer.getMessagePoller().commitMinIndex(subscribeTopic, partition);
    }

    @Override
    public FetchIndexData fetchIndex(short partition) {
        checkState();
        checkSubscribe();

        return topicMessageConsumer.getMessagePoller().fetchIndex(subscribeTopic, partition);
    }

    @Override
    public TopicMetadata getTopicMetadata(String topic) {
        checkState();
        Preconditions.checkArgument(StringUtils.isNotBlank(topic), "topic not blank");

        String topicFullName = NameServerHelper.getTopicFullName(topic, nameServerConfig);
        return clusterManager.fetchTopicMetadata(topicFullName, config.getAppFullName());
    }

    protected void checkState() {
        if (!isStarted()) {
            throw new ConsumerException("consumer is not started", JoyQueueCode.CN_SERVICE_NOT_AVAILABLE.getCode());
        }
    }

    protected void checkSubscribe() {
        if (StringUtils.isBlank(subscribeTopic)) {
            throw new ConsumerException("consumer not subscribe topic", JoyQueueCode.CN_SERVICE_NOT_AVAILABLE.getCode());
        }
    }

    protected void checkUnsubscribe() {
        if (StringUtils.isNotBlank(subscribeTopic)) {
            throw new ConsumerException("consumer is subscribed topic", JoyQueueCode.CN_SERVICE_NOT_AVAILABLE.getCode());
        }
    }
}