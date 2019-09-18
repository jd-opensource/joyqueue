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
package io.chubao.joyqueue.nsr.ignite.service;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import io.chubao.joyqueue.domain.Consumer;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.event.ConsumerEvent;
import io.chubao.joyqueue.event.MetaEvent;
import io.chubao.joyqueue.nsr.ignite.dao.ConsumerConfigDao;
import io.chubao.joyqueue.nsr.ignite.dao.ConsumerDao;
import io.chubao.joyqueue.nsr.ignite.message.IgniteMessenger;
import io.chubao.joyqueue.nsr.ignite.model.IgniteConsumer;
import io.chubao.joyqueue.nsr.ignite.model.IgniteConsumerConfig;
import io.chubao.joyqueue.nsr.model.ConsumerQuery;
import io.chubao.joyqueue.nsr.service.internal.ConsumerInternalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        ConsumerQuery consumerQuery = new ConsumerQuery(app);
        List<IgniteConsumer> igniteConsumers = consumerDao.list(consumerQuery);
        if (null == igniteConsumers || igniteConsumers.size() < 1) Collections.emptyList();
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
