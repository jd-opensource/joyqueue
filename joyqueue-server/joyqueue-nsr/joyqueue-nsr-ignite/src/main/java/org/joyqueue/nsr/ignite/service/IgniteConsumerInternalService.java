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
package org.joyqueue.nsr.ignite.service;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import org.joyqueue.domain.Consumer;
import org.joyqueue.domain.TopicName;
import org.joyqueue.event.ConsumerEvent;
import org.joyqueue.event.MetaEvent;
import org.joyqueue.nsr.ignite.dao.ConsumerConfigDao;
import org.joyqueue.nsr.ignite.dao.ConsumerDao;
import org.joyqueue.nsr.ignite.message.IgniteMessenger;
import org.joyqueue.nsr.ignite.model.IgniteConsumer;
import org.joyqueue.nsr.ignite.model.IgniteConsumerConfig;
import org.joyqueue.nsr.model.ConsumerQuery;
import org.joyqueue.nsr.service.internal.ConsumerInternalService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author wylixiaobin
 * Date: 2018/9/4
 */
public class IgniteConsumerInternalService implements ConsumerInternalService {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected ConsumerConfigDao consumerConfigDao;
    protected ConsumerDao consumerDao;

    @Inject
    public IgniteConsumerInternalService(ConsumerDao consumerDao, ConsumerConfigDao consumerConfigDao) {
        this.consumerDao = consumerDao;
        this.consumerConfigDao = consumerConfigDao;
    }

    @Inject
    protected IgniteMessenger messenger;

    @Override
    public Consumer getById(String id) {
        IgniteConsumer consumer = consumerDao.findById(id);
        if (null != consumer) {
            consumer.fillConfig(consumerConfigDao.findById(id));
        }
        return consumer;
    }

    @Override
    public Consumer getByTopicAndApp(TopicName topic, String app) {
        return getById(IgniteConsumer.getId(topic, app));
    }

    @Override
    public List<Consumer> getByTopic(TopicName topic) {
        ConsumerQuery consumerQuery = new ConsumerQuery(topic.getCode(), topic.getNamespace());
        List<IgniteConsumer> igniteConsumers = consumerDao.list(consumerQuery);
        if (null == igniteConsumers || igniteConsumers.size() < 1) return Collections.emptyList();
        Map<String, IgniteConsumerConfig> configs = new HashMap<>();
        List<IgniteConsumerConfig> igniteConsumerConfigs = consumerConfigDao.list(consumerQuery);
        if (null != igniteConsumerConfigs && igniteConsumerConfigs.size() > 0) {
            igniteConsumerConfigs.forEach(config -> {
                configs.put(config.getId(), config);
            });
        }
        List<Consumer> consumers = new ArrayList<>();
        igniteConsumers.forEach(consumer -> {
            consumers.add(consumer.fillConfig(configs.get(consumer.getId())));
        });
        return consumers;
    }

    @Override
    public List<Consumer> getByApp(String app) {
        Set<IgniteConsumer> consumers = Sets.newHashSet();

        ConsumerQuery referConsumerQuery = new ConsumerQuery();
        referConsumerQuery.setReferer(app);
        List<IgniteConsumer> refererConsumers = consumerDao.list(referConsumerQuery);

        ConsumerQuery appConsumerQuery = new ConsumerQuery();
        appConsumerQuery.setApp(app);
        List<IgniteConsumer> appConsumers = consumerDao.list(appConsumerQuery);

        if (CollectionUtils.isNotEmpty(refererConsumers)) {
            consumers.addAll(refererConsumers);
        }

        if (CollectionUtils.isNotEmpty(appConsumers)) {
            consumers.addAll(appConsumers);
        }

        if (null == consumers || consumers.size() < 1) Collections.emptyList();
        Map<String, IgniteConsumerConfig> configs = new HashMap<>();
        List<IgniteConsumerConfig> refererConsumerConfigs = consumerConfigDao.list(referConsumerQuery);
        List<IgniteConsumerConfig> appConsumerConfigs = consumerConfigDao.list(appConsumerQuery);

        if (CollectionUtils.isNotEmpty(refererConsumerConfigs)) {
            refererConsumerConfigs.forEach(config -> {
                configs.put(config.getId(), config);
            });
        }

        if (CollectionUtils.isNotEmpty(appConsumerConfigs)) {
            appConsumerConfigs.forEach(config -> {
                configs.put(config.getId(), config);
            });
        }

        List<Consumer> result = new ArrayList<>();
        consumers.forEach(consumer -> {
            result.add(consumer.fillConfig(configs.get(consumer.getId())));
        });
        return result;
    }

    public IgniteConsumer toIgniteModel(Consumer model) {
        return new IgniteConsumer(model);
    }

    @Override
    public void delete(String id) {
        consumerDao.deleteById(id);
    }

    @Override
    public List<Consumer> getAll() {
        List<IgniteConsumer> consumers = consumerDao.list(null);
        List<IgniteConsumerConfig> consumerConfigs = consumerConfigDao.list(null);
        Map<String, IgniteConsumerConfig> consumerConfigMap = Maps.newHashMap();

        for (IgniteConsumerConfig consumerConfig : consumerConfigs) {
            consumerConfigMap.put(consumerConfig.getId(), consumerConfig);
        }

        for (IgniteConsumer consumer : consumers) {
            IgniteConsumerConfig igniteConsumerConfig = consumerConfigMap.get(consumer.getId());
            if (igniteConsumerConfig != null) {
                consumer.fillConfig(igniteConsumerConfig);
            }
        }

        return convert(consumers);
    }

    public List<Consumer> getConsumerByClientType(byte clientType) {
        ConsumerQuery query = new ConsumerQuery();
        query.setClientType(clientType);
        return convert(consumerDao.list(query));
    }

    @Override
    public Consumer add(Consumer consumer) {
        try {
            IgniteConsumer igConsumer = toIgniteModel(consumer);
            consumerDao.addOrUpdate(igConsumer);
            if (null != consumer.getConsumerPolicy() || null != consumer.getRetryPolicy() || consumer.getLimitPolicy() != null) {
                consumerConfigDao.addOrUpdate(new IgniteConsumerConfig(igConsumer));
            }
            this.publishEvent(ConsumerEvent.add(consumer.getTopic(), consumer.getApp()));
            return consumer;
        } catch (Exception e) {
            String message = String.format("add consumer [%s] ,topic [%s] error", consumer.getApp(), consumer.getTopic());
            logger.error(message, e);
            throw new RuntimeException(message, e);
        }
    }

    @Override
    public Consumer update(Consumer consumer) {
        try {
            IgniteConsumer igConsumer = toIgniteModel(consumer);
            consumerDao.addOrUpdate(igConsumer);
            if (null != consumer.getConsumerPolicy() || null != consumer.getRetryPolicy() || consumer.getLimitPolicy() != null) {
                consumerConfigDao.addOrUpdate(new IgniteConsumerConfig(igConsumer));
            }
            this.publishEvent(ConsumerEvent.add(consumer.getTopic(), consumer.getApp()));
            return consumer;
        } catch (Exception e) {
            String message = String.format("add consumer [%s] ,topic [%s] error", consumer.getApp(), consumer.getTopic());
            logger.error(message, e);
            throw new RuntimeException(message, e);
        }
    }

    public List<Consumer> list(ConsumerQuery query) {
        return convert(consumerDao.list(query));
    }

    public void publishEvent(MetaEvent event) {
        messenger.publish(event);
    }


    List<Consumer> convert(List<IgniteConsumer> consumers) {
        List<Consumer> result = new ArrayList<>();
        if (consumers != null) {
            result.addAll(consumers);
        }

        return result;
    }
}
