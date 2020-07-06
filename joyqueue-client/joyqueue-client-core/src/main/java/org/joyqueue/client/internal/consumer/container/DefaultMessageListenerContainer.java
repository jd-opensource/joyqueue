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
package org.joyqueue.client.internal.consumer.container;

import com.google.common.collect.Maps;
import org.joyqueue.client.internal.cluster.ClusterClientManager;
import org.joyqueue.client.internal.cluster.ClusterManager;
import org.joyqueue.client.internal.consumer.BaseMessageListener;
import org.joyqueue.client.internal.consumer.BatchMessageListener;
import org.joyqueue.client.internal.consumer.MessageListener;
import org.joyqueue.client.internal.consumer.MessageListenerContainer;
import org.joyqueue.client.internal.consumer.config.ConsumerConfig;
import org.joyqueue.client.internal.consumer.exception.ConsumerException;
import org.joyqueue.client.internal.consumer.support.TopicMessageConsumer;
import org.joyqueue.client.internal.consumer.transport.ConsumerClientManager;
import org.joyqueue.client.internal.nameserver.NameServerConfig;
import org.joyqueue.exception.JoyQueueCode;
import com.google.common.base.Preconditions;
import org.joyqueue.toolkit.service.Service;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * DefaultMessageListenerContainer
 *
 * author: gaohaoxiang
 * date: 2018/12/25
 */
public class DefaultMessageListenerContainer extends Service implements MessageListenerContainer {

    protected static final Logger logger = LoggerFactory.getLogger(DefaultMessageListenerContainer.class);

    private ConsumerConfig config;
    private NameServerConfig nameServerConfig;
    private ClusterManager clusterManager;
    private ClusterClientManager clusterClientManager;
    private ConsumerClientManager consumerClientManager;
    private Map<String, TopicMessageConsumer> topicConsumerMap = Maps.newHashMap();

    public DefaultMessageListenerContainer(ConsumerConfig config, NameServerConfig nameServerConfig, ClusterManager clusterManager,
                                           ClusterClientManager clusterClientManager, ConsumerClientManager consumerClientManager) {
        Preconditions.checkArgument(config != null, "consumer not null");
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
        for (Map.Entry<String, TopicMessageConsumer> entry : topicConsumerMap.entrySet()) {
            if (!entry.getValue().isStarted()) {
                entry.getValue().start();
            }
        }
//        logger.info("consumer container is started");
    }

    @Override
    protected void doStop() {
        for (Map.Entry<String, TopicMessageConsumer> entry : topicConsumerMap.entrySet()) {
            entry.getValue().stop();
        }
//        logger.info("consumer container is stopped");
    }

    @Override
    public synchronized void addListener(String topic, MessageListener messageListener) {
        doAddListener(topic, messageListener);
    }

    @Override
    public synchronized void addBatchListener(String topic, BatchMessageListener messageListener) {
        doAddListener(topic, messageListener);
    }

    protected void doAddListener(String topic, BaseMessageListener messageListener) {
        Preconditions.checkArgument(StringUtils.isNotBlank(topic), "topic not blank");

        TopicMessageConsumer topicMessageConsumer = topicConsumerMap.get(topic);
        if (topicMessageConsumer == null) {
            topicMessageConsumer = createTopicMessageConsumer(topic);
            topicConsumerMap.put(topic, topicMessageConsumer);

            if (isStarted()) {
                try {
                    topicMessageConsumer.start();
                } catch (Exception e) {
                    logger.error("start topic message consumer exception, topic : {}", topic, e);
                    throw new ConsumerException("start message consumer exception", JoyQueueCode.CN_SERVICE_NOT_AVAILABLE.getCode());
                }
            }
        }

        topicMessageConsumer.addListener(messageListener);
    }

    protected TopicMessageConsumer createTopicMessageConsumer(String topic) {
        return new TopicMessageConsumer(topic, config, nameServerConfig, clusterManager, clusterClientManager, consumerClientManager);
    }
}