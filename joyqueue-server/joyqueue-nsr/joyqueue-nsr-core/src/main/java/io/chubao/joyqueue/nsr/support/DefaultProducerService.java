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
package io.chubao.joyqueue.nsr.support;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.chubao.joyqueue.domain.Broker;
import io.chubao.joyqueue.domain.PartitionGroup;
import io.chubao.joyqueue.domain.Producer;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.nsr.event.AddProducerEvent;
import io.chubao.joyqueue.nsr.event.RemoveProducerEvent;
import io.chubao.joyqueue.nsr.event.UpdateProducerEvent;
import io.chubao.joyqueue.nsr.exception.NsrException;
import io.chubao.joyqueue.nsr.message.Messenger;
import io.chubao.joyqueue.nsr.service.ProducerService;
import io.chubao.joyqueue.nsr.service.internal.BrokerInternalService;
import io.chubao.joyqueue.nsr.service.internal.PartitionGroupInternalService;
import io.chubao.joyqueue.nsr.service.internal.ProducerInternalService;
import io.chubao.joyqueue.nsr.service.internal.TopicInternalService;
import io.chubao.joyqueue.nsr.service.internal.TransactionInternalService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * DefaultProducerService
 * author: gaohaoxiang
 * date: 2019/8/27
 */
public class DefaultProducerService implements ProducerService {

    protected static final Logger logger = LoggerFactory.getLogger(DefaultProducerService.class);

    private TopicInternalService topicInternalService;
    private PartitionGroupInternalService partitionGroupInternalService;
    private BrokerInternalService brokerInternalService;
    private ProducerInternalService producerInternalService;
    private TransactionInternalService transactionInternalService;
    private Messenger messenger;

    public DefaultProducerService(TopicInternalService topicInternalService, PartitionGroupInternalService partitionGroupInternalService,
                                  BrokerInternalService brokerInternalService, ProducerInternalService producerInternalService,
                                  TransactionInternalService transactionInternalService, Messenger messenger) {
        this.topicInternalService = topicInternalService;
        this.partitionGroupInternalService = partitionGroupInternalService;
        this.brokerInternalService = brokerInternalService;
        this.producerInternalService = producerInternalService;
        this.transactionInternalService = transactionInternalService;
        this.messenger = messenger;
    }

    @Override
    public Producer getById(String id) {
        return producerInternalService.getById(id);
    }

    @Override
    public Producer getByTopicAndApp(TopicName topic, String app) {
        return producerInternalService.getByTopicAndApp(topic, app);
    }

    @Override
    public List<Producer> getByTopic(TopicName topic) {
        return producerInternalService.getByTopic(topic);
    }

    @Override
    public List<Producer> getByApp(String app) {
        return producerInternalService.getByApp(app);
    }

    @Override
    public List<Producer> getAll() {
        return producerInternalService.getAll();
    }

    @Override
    public Producer add(Producer producer) {
        if (topicInternalService.getTopicByCode(producer.getTopic().getNamespace(), producer.getTopic().getCode()) == null) {
            throw new NsrException(String.format("topic: %s is not exist", producer.getTopic()));
        }
        if (producerInternalService.getByTopicAndApp(producer.getTopic(), producer.getApp()) != null) {
            throw new NsrException(String.format("producer: %s,%s is not exist", producer.getTopic(), producer.getApp()));
        }

        logger.info("addProducer, topic: {}, app: {}", producer.getTopic(), producer.getApp());

        List<PartitionGroup> partitionGroups = partitionGroupInternalService.getByTopic(producer.getTopic());
        List<Broker> replicas = getReplicas(partitionGroups);

        try {
            transactionInternalService.begin();
        } catch (Exception e) {
            logger.error("beginTransaction exception, topic: {}, app: {}", producer.getTopic(), producer.getApp(), e);
            throw new NsrException(e);
        }

        try {
            producerInternalService.add(producer);
            transactionInternalService.commit();
        } catch (Exception e) {
            logger.error("addProducer exception, topic: {}, app: {}", producer.getTopic(), producer.getApp(), e);
            transactionInternalService.rollback();
            throw new NsrException(e);
        }

        messenger.publish(new AddProducerEvent(producer.getTopic(), producer), replicas);
        return producer;
    }

    @Override
    public Producer update(Producer producer) {
        Producer oldProducer = producerInternalService.getByTopicAndApp(producer.getTopic(), producer.getApp());
        if (oldProducer == null) {
            throw new NsrException(String.format("topic: %s, producer: %s is not exist", producer.getTopic(), producer.getApp()));
        }

        logger.info("updateProducer, topic: {}, app: {}", producer.getTopic(), producer.getApp());

        List<PartitionGroup> partitionGroups = partitionGroupInternalService.getByTopic(producer.getTopic());
        List<Broker> replicas = getReplicas(partitionGroups);

        try {
            transactionInternalService.begin();
        } catch (Exception e) {
            logger.error("beginTransaction exception, topic: {}, app: {}", producer.getTopic(), producer.getApp(), e);
            throw new NsrException(e);
        }

        try {
            producerInternalService.update(producer);
            transactionInternalService.commit();
        } catch (Exception e) {
            logger.error("updateProducer exception, topic: {}, app: {}", producer.getTopic(), producer.getApp(), e);
            transactionInternalService.rollback();
            throw new NsrException(e);
        }

        messenger.publish(new UpdateProducerEvent(producer.getTopic(), oldProducer, producer), replicas);
        return producer;
    }

    @Override
    public void delete(String id) {
        Producer producer = producerInternalService.getById(id);
        if (producer == null) {
            throw new NsrException(String.format("producer: %s is not exist", id));
        }

        logger.info("deleteProducer, topic: {}, app: {}", producer.getTopic(), producer.getApp());

        List<PartitionGroup> partitionGroups = partitionGroupInternalService.getByTopic(producer.getTopic());
        List<Broker> replicas = getReplicas(partitionGroups);

        try {
            transactionInternalService.begin();
        } catch (Exception e) {
            logger.error("beginTransaction exception, topic: {}, app: {}", producer.getTopic(), producer.getApp(), e);
            throw new NsrException(e);
        }

        try {
            producerInternalService.delete(id);
            transactionInternalService.commit();
        } catch (Exception e) {
            logger.error("deleteProducer exception, topic: {}, app: {}", producer.getTopic(), producer.getApp(), e);
            transactionInternalService.rollback();
            throw new NsrException(e);
        }

        messenger.publish(new RemoveProducerEvent(producer.getTopic(), producer), replicas);
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