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
import com.google.inject.Inject;
import org.joyqueue.domain.Producer;
import org.joyqueue.domain.TopicName;
import org.joyqueue.event.MetaEvent;
import org.joyqueue.event.ProducerEvent;
import org.joyqueue.nsr.ignite.dao.ProducerConfigDao;
import org.joyqueue.nsr.ignite.dao.ProducerDao;
import org.joyqueue.nsr.ignite.message.IgniteMessenger;
import org.joyqueue.nsr.ignite.model.IgniteProducer;
import org.joyqueue.nsr.ignite.model.IgniteProducerConfig;
import org.joyqueue.nsr.model.ProducerQuery;
import org.joyqueue.nsr.service.internal.ProducerInternalService;
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
public class IgniteProducerInternalService implements ProducerInternalService {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected ProducerConfigDao producerConfigDao;
    protected ProducerDao producerDao;

    @Inject
    public IgniteProducerInternalService(ProducerDao igniteDao, ProducerConfigDao producerConfigDao) {
        this.producerConfigDao = producerConfigDao;
        this.producerDao = igniteDao;
    }

    @Inject
    protected IgniteMessenger messenger;

    @Override
    public Producer getByTopicAndApp(TopicName topic, String app) {
        return getById(IgniteProducer.getId(topic, app));
    }

    @Override
    public List<Producer> getByTopic(TopicName topic) { ProducerQuery producerQuery = new ProducerQuery(topic.getCode(), topic.getNamespace());
        List<IgniteProducer> igniteProducers = producerDao.list(producerQuery);
        if (null == igniteProducers || igniteProducers.size() < 1) {
            return Collections.emptyList();
        }
        Map<String, IgniteProducerConfig> configs = new HashMap<>();
        List<IgniteProducerConfig> igniteProducerConfigs = producerConfigDao.list(producerQuery);
        if (null != igniteProducerConfigs && igniteProducerConfigs.size() > 0) {
            igniteProducerConfigs.forEach(config -> {
                configs.put(config.getId(), config);
            });
        }
        List<Producer> producers = new ArrayList<>();
        igniteProducers.forEach(producer -> {
            producers.add(producer.fillConfig(configs.get(producer.getId())));
        });
        return producers;
    }

    @Override
    public List<Producer> getByApp(String app) {
        ProducerQuery producerQuery = new ProducerQuery(app);
        List<IgniteProducer> igniteProducers = producerDao.list(producerQuery);
        if (null == igniteProducers || igniteProducers.size() < 1) {
            return Collections.emptyList();
        }
        Map<String, IgniteProducerConfig> configs = new HashMap<>();
        List<IgniteProducerConfig> igniteProducerConfigs = producerConfigDao.list(producerQuery);
        if (null != igniteProducerConfigs && igniteProducerConfigs.size() > 0) {
            igniteProducerConfigs.forEach(config -> {
                configs.put(config.getId(), config);
            });
        }
        List<Producer> producers = new ArrayList<>();
        igniteProducers.forEach(producer -> {
            producers.add(producer.fillConfig(configs.get(producer.getId())));
        });
        return producers;
    }

    @Override
    public Producer getById(String id) {
        IgniteProducer producer = producerDao.findById(id);
        if(producer != null) {
            producer.fillConfig(producerConfigDao.findById(id));
        }
        return producer;
    }

    @Override
    public List<Producer> getAll() {
        List<IgniteProducer> producers = producerDao.list(null);
        List<IgniteProducerConfig> producerConfigs = producerConfigDao.list(null);
        Map<String, IgniteProducerConfig> producerConfigMap = Maps.newHashMap();

        for (IgniteProducerConfig producerConfig : producerConfigs) {
            producerConfigMap.put(producerConfig.getId(), producerConfig);
        }

        for (IgniteProducer producer : producers) {
            IgniteProducerConfig igniteProducerConfig = producerConfigMap.get(producer.getId());
            if (igniteProducerConfig != null) {
                producer.fillConfig(igniteProducerConfig);
            }
        }

        return convert(producers);
    }

    @Override
    public Producer add(Producer producer) {
        try {
            producerDao.addOrUpdate(toIgniteModel(producer));
            if (null != producer.getProducerPolicy() || producer.getLimitPolicy() != null) {
                producerConfigDao.addOrUpdate(new IgniteProducerConfig(new IgniteProducer(producer)));
            }
            this.publishEvent(ProducerEvent.add(producer.getTopic(), producer.getApp()));
            return producer;
        } catch (Exception e) {
            throw new RuntimeException("producerService addOrUpdate error", e);
        }
    }

    @Override
    public Producer update(Producer producer) {
        try {
            producerDao.addOrUpdate(toIgniteModel(producer));
            if (null != producer.getProducerPolicy() || producer.getLimitPolicy() != null) {
                producerConfigDao.addOrUpdate(new IgniteProducerConfig(new IgniteProducer(producer)));
            }
            this.publishEvent(ProducerEvent.add(producer.getTopic(), producer.getApp()));
            return producer;
        } catch (Exception e) {
            throw new RuntimeException("producerService addOrUpdate error", e);
        }
    }

    public IgniteProducer toIgniteModel(Producer model) {
        return new IgniteProducer(model);
    }

    @Override
    public void delete(String id) {
        Producer producer = getById(id);
        try {
            producerDao.deleteById(id);
            producerConfigDao.deleteById(id);
            this.publishEvent(ProducerEvent.remove(producer.getTopic(), producer.getApp()));
        } catch (Exception e) {
            throw new RuntimeException("delete producer error.", e);
        }
    }

    public void publishEvent(MetaEvent event) {
        messenger.publish(event);
    }

    public List<Producer> list(ProducerQuery query) {
        return convert(producerDao.list(query));
    }

    public List<Producer> getProducerByClientType(byte clientType) {
        ProducerQuery query = new ProducerQuery();
        query.setClientType(clientType);
        return convert(producerDao.list(query));
    }


    public static final List<Producer> convert(List<IgniteProducer> from) {
        if (from != null && !from.isEmpty()) {
            List<Producer> resultData = new ArrayList<>();
            from.forEach(e -> resultData.add(e));
            return resultData;
        }

        return Collections.emptyList();
    }
}
