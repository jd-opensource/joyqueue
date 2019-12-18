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
package io.chubao.joyqueue.client.internal.consumer.support;

import io.chubao.joyqueue.client.internal.cluster.ClusterClientManager;
import io.chubao.joyqueue.client.internal.cluster.ClusterManager;
import io.chubao.joyqueue.client.internal.consumer.BaseMessageListener;
import io.chubao.joyqueue.client.internal.consumer.MessagePoller;
import io.chubao.joyqueue.client.internal.consumer.MessagePollerFactory;
import io.chubao.joyqueue.client.internal.consumer.config.ConsumerConfig;
import io.chubao.joyqueue.client.internal.consumer.coordinator.ConsumerCoordinator;
import io.chubao.joyqueue.client.internal.consumer.exception.ConsumerException;
import io.chubao.joyqueue.client.internal.consumer.interceptor.ConsumerInterceptor;
import io.chubao.joyqueue.client.internal.consumer.interceptor.ConsumerInterceptorManager;
import io.chubao.joyqueue.client.internal.consumer.transport.ConsumerClientManager;
import io.chubao.joyqueue.client.internal.metadata.domain.TopicMetadata;
import io.chubao.joyqueue.client.internal.nameserver.NameServerConfig;
import io.chubao.joyqueue.client.internal.nameserver.helper.NameServerHelper;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.domain.TopicType;
import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TopicMessageConsumerGroup
 *
 * author: gaohaoxiang
 * date: 2018/12/25
 */
public class TopicMessageConsumer extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(ConsumerCoordinator.class);

    private String topic;
    private ConsumerConfig config;
    private NameServerConfig nameServerConfig;
    private ClusterManager clusterManager;
    private ClusterClientManager clusterClientManager;
    private ConsumerClientManager consumerClientManager;
    private ConsumerInterceptorManager consumerInterceptorManager;

    private MessagePoller messagePoller;
    private TopicMessageConsumerDispatcher messageConsumerDispatcher;
    private TopicMessageConsumerScheduler messageConsumerScheduler;
    private MessageListenerManager messageListenerManager = new MessageListenerManager();
    private String appFullName;

    public TopicMessageConsumer(String topic, ConsumerConfig config, NameServerConfig nameServerConfig, ClusterManager clusterManager,
                                ClusterClientManager clusterClientManager, ConsumerClientManager consumerClientManager) {
        this(topic, config, nameServerConfig, clusterManager, clusterClientManager, consumerClientManager, new ConsumerInterceptorManager());
    }

    public TopicMessageConsumer(String topic, ConsumerConfig config, NameServerConfig nameServerConfig, ClusterManager clusterManager,
                                ClusterClientManager clusterClientManager, ConsumerClientManager consumerClientManager, ConsumerInterceptorManager consumerInterceptorManager) {
        this.topic = topic;
        this.config = config;
        this.nameServerConfig = nameServerConfig;
        this.clusterManager = clusterManager;
        this.clusterClientManager = clusterClientManager;
        this.consumerClientManager = consumerClientManager;
        this.consumerInterceptorManager = consumerInterceptorManager;
    }

    @Override
    protected void validate() throws Exception {
        messagePoller = createMessageConsumer(topic);
        messageConsumerDispatcher = new TopicMessageConsumerDispatcher(topic, config, nameServerConfig, messagePoller, messageListenerManager, consumerInterceptorManager);
        messageConsumerScheduler = new TopicMessageConsumerScheduler(topic, config, messagePoller, messageConsumerDispatcher);
    }

    @Override
    protected void doStart() throws Exception {
        messagePoller.start();
        if (!messageListenerManager.isEmpty()) {
            messageConsumerDispatcher.start();
            messageConsumerScheduler.start();
        }
    }

    @Override
    protected void doStop() {
        if (!messageListenerManager.isEmpty()) {
            if (messageConsumerScheduler != null) {
                messageConsumerScheduler.stop();
            }
            if (messageConsumerDispatcher != null) {
                messageConsumerDispatcher.stop();
            }
        }
        if (messagePoller != null) {
            messagePoller.stop();
        }
    }

    public void suspend() {
        messageConsumerScheduler.suspend();
    }

    public boolean isSuspend() {
        return messageConsumerScheduler.isSuspend();
    }

    public void resume() {
        messageConsumerScheduler.resume();
    }

    protected MessagePoller createMessageConsumer(String topic) {
        TopicName topicName = TopicName.parse(NameServerHelper.getTopicFullName(topic, nameServerConfig));
        TopicMetadata topicMetadata = clusterManager.fetchTopicMetadata(topicName.getFullName(), config.getAppFullName());

        if (topicMetadata == null || topicMetadata.getConsumerPolicy() == null) {
            throw new ConsumerException(String.format("topic %s is not exist", topic), JoyQueueCode.FW_TOPIC_NOT_EXIST.getCode());
        }

        if (topicMetadata.getType().equals(TopicType.BROADCAST)) {
            return MessagePollerFactory.createBroadcastPoller(config, nameServerConfig, clusterManager, consumerClientManager);
        } else {
            return MessagePollerFactory.create(config, nameServerConfig, clusterManager, clusterClientManager, consumerClientManager);
        }
    }

    public synchronized void addInterceptor(ConsumerInterceptor consumerInterceptor) {
        consumerInterceptorManager.addInterceptor(consumerInterceptor);
    }

    public synchronized void removeInterceptor(ConsumerInterceptor consumerInterceptor) {
        consumerInterceptorManager.removeInterceptor(consumerInterceptor);
    }

    public synchronized void addListener(BaseMessageListener messageListener) {
        boolean isEmpty = messageListenerManager.isEmpty();
        messageListenerManager.addListener(messageListener);
        if (isStarted() && isEmpty) {
            try {
                messageConsumerDispatcher.start();
                messageConsumerScheduler.start();
            } catch (Exception e) {
                throw new ConsumerException(e);
            }
        }
    }

    public synchronized void removeListener(BaseMessageListener messageListener) {
        messageListenerManager.removeListener(messageListener);
    }

    public MessagePoller getMessagePoller() {
        return messagePoller;
    }
}