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
package org.joyqueue.nsr.support;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.joyqueue.domain.Broker;
import org.joyqueue.domain.Consumer;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.TopicName;
import org.joyqueue.nsr.config.NameServiceConfig;
import org.joyqueue.nsr.event.AddConsumerEvent;
import org.joyqueue.nsr.event.RemoveConsumerEvent;
import org.joyqueue.nsr.event.UpdateConsumerEvent;
import org.joyqueue.nsr.exception.NsrException;
import org.joyqueue.nsr.message.Messenger;
import org.joyqueue.nsr.service.ConsumerService;
import org.joyqueue.nsr.service.internal.BrokerInternalService;
import org.joyqueue.nsr.service.internal.ConsumerInternalService;
import org.joyqueue.nsr.service.internal.PartitionGroupInternalService;
import org.joyqueue.nsr.service.internal.TopicInternalService;
import org.joyqueue.nsr.service.internal.TransactionInternalService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * DefaultConsumerService
 * author: gaohaoxiang
 * date: 2019/8/27
 */
public class DefaultConsumerService implements ConsumerService {

    protected static final Logger logger = LoggerFactory.getLogger(DefaultConsumerService.class);

    private TopicInternalService topicInternalService;
    private PartitionGroupInternalService partitionGroupInternalService;
    private BrokerInternalService brokerInternalService;
    private ConsumerInternalService consumerInternalService;
    private TransactionInternalService transactionInternalService;
    private NameServiceConfig config;
    private Messenger messenger;

    public DefaultConsumerService(TopicInternalService topicInternalService, PartitionGroupInternalService partitionGroupInternalService,
                                  BrokerInternalService brokerInternalService, ConsumerInternalService consumerInternalService,
                                  TransactionInternalService transactionInternalService, NameServiceConfig config, Messenger messenger) {
        this.topicInternalService = topicInternalService;
        this.partitionGroupInternalService = partitionGroupInternalService;
        this.brokerInternalService = brokerInternalService;
        this.consumerInternalService = consumerInternalService;
        this.transactionInternalService = transactionInternalService;
        this.config = config;
        this.messenger = messenger;
    }

    @Override
    public Consumer getById(String id) {
        return consumerInternalService.getById(id);
    }

    @Override
    public Consumer getByTopicAndApp(TopicName topic, String app) {
        return consumerInternalService.getByTopicAndApp(topic, app);
    }

    @Override
    public List<Consumer> getByTopic(TopicName topic) {
        return consumerInternalService.getByTopic(topic);
    }

    @Override
    public List<Consumer> getByApp(String app) {
        return consumerInternalService.getByApp(app);
    }

    @Override
    public List<Consumer> getAll() {
        return consumerInternalService.getAll();
    }

    @Override
    public Consumer add(Consumer consumer) {
        if (topicInternalService.getTopicByCode(consumer.getTopic().getNamespace(), consumer.getTopic().getCode()) == null) {
            throw new NsrException(String.format("topic: %s does not exist", consumer.getTopic()));
        }
        if (consumerInternalService.getByTopicAndApp(consumer.getTopic(), consumer.getApp()) != null) {
            throw new NsrException(String.format("consumer: %s,%s is exist", consumer.getTopic(), consumer.getApp()));
        }

        logger.info("addConsumer, topic: {}, app: {}", consumer.getTopic(), consumer.getApp());

        List<PartitionGroup> partitionGroups = partitionGroupInternalService.getByTopic(consumer.getTopic());
        List<Broker> replicas = getReplicas(partitionGroups);

        try {
            transactionInternalService.begin();
        } catch (Exception e) {
            logger.error("beginTransaction exception, topic: {}, app: {}", consumer.getTopic(), consumer.getApp(), e);
            throw new NsrException(e);
        }

        try {
            consumerInternalService.add(consumer);
            transactionInternalService.commit();
        } catch (Exception e) {
            logger.error("addConsumer exception, topic: {}, app: {}", consumer.getTopic(), consumer.getApp(), e);
            transactionInternalService.rollback();
            throw new NsrException(e);
        }

        if (config.getMessengerPublishSubscriptionEnable()) {
            messenger.publish(new AddConsumerEvent(consumer.getTopic(), consumer), replicas);
        }
        return consumer;
    }

    @Override
    public Consumer update(Consumer consumer) {
        Consumer oldConsumer = consumerInternalService.getByTopicAndApp(consumer.getTopic(), consumer.getApp());
        if (oldConsumer == null) {
            throw new NsrException(String.format("topic: %s, consumer: %s does not exist", oldConsumer.getTopic(), oldConsumer.getApp()));
        }

        logger.info("updateConsumer, topic: {}, app: {}", consumer.getTopic(), consumer.getApp());

        List<PartitionGroup> partitionGroups = partitionGroupInternalService.getByTopic(consumer.getTopic());
        List<Broker> replicas = getReplicas(partitionGroups);

        try {
            transactionInternalService.begin();
        } catch (Exception e) {
            logger.error("beginTransaction exception, topic: {}, app: {}", consumer.getTopic(), consumer.getApp(), e);
            throw new NsrException(e);
        }

        try {
            consumerInternalService.update(consumer);
            transactionInternalService.commit();
        } catch (Exception e) {
            logger.error("updateConsumer exception, topic: {}, app: {}", consumer.getTopic(), consumer.getApp(), e);
            transactionInternalService.rollback();
            throw new NsrException(e);
        }

        if (config.getMessengerPublishSubscriptionEnable()) {
            messenger.publish(new UpdateConsumerEvent(consumer.getTopic(), oldConsumer, consumer), replicas);
        }
        return consumer;
    }

    @Override
    public void delete(String id) {
        Consumer consumer = consumerInternalService.getById(id);
        if (consumer == null) {
            throw new NsrException(String.format("consumer: %s does not exist", id));
        }

        logger.info("deleteConsumer, topic: {}, app: {}", consumer.getTopic(), consumer.getApp());

        List<PartitionGroup> partitionGroups = partitionGroupInternalService.getByTopic(consumer.getTopic());
        List<Broker> replicas = getReplicas(partitionGroups);

        try {
            transactionInternalService.begin();
        } catch (Exception e) {
            logger.error("beginTransaction exception, topic: {}, app: {}", consumer.getTopic(), consumer.getApp(), e);
            throw new NsrException(e);
        }

        try {
            consumerInternalService.delete(id);
            transactionInternalService.commit();
        } catch (Exception e) {
            logger.error("deleteConsumer exception, topic: {}, app: {}", consumer.getTopic(), consumer.getApp(), e);
            transactionInternalService.rollback();
            throw new NsrException(e);
        }

        if (config.getMessengerPublishSubscriptionEnable()) {
            messenger.publish(new RemoveConsumerEvent(consumer.getTopic(), consumer), replicas);
        }
    }

    protected List<Broker> getReplicas(List<PartitionGroup> partitionGroups) {
        if (CollectionUtils.isEmpty(partitionGroups)) {
            return null;
        }
        Set<Integer> replicaIds = Sets.newHashSet();
        for (PartitionGroup partitionGroup : partitionGroups) {
            if (partitionGroup.getReplicas() != null) {
                replicaIds.addAll(partitionGroup.getReplicas());
            }
            if (partitionGroup.getLearners() != null) {
                replicaIds.addAll(partitionGroup.getLearners());
            }
        }
        return brokerInternalService.getByIds(Lists.newArrayList(replicaIds));
    }
}
