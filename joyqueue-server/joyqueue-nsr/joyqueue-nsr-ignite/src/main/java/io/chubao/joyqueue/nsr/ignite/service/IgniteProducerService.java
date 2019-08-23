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

import com.google.inject.Inject;
import io.chubao.joyqueue.domain.Producer;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.event.MetaEvent;
import io.chubao.joyqueue.event.ProducerEvent;
import io.chubao.joyqueue.nsr.ignite.dao.ProducerConfigDao;
import io.chubao.joyqueue.nsr.ignite.dao.ProducerDao;
import io.chubao.joyqueue.nsr.ignite.model.IgniteProducer;
import io.chubao.joyqueue.nsr.ignite.model.IgniteProducerConfig;
import io.chubao.joyqueue.nsr.message.Messenger;
import io.chubao.joyqueue.nsr.model.ProducerQuery;
import io.chubao.joyqueue.nsr.service.ProducerService;
import org.apache.ignite.Ignition;
import org.apache.ignite.transactions.Transaction;
import org.apache.ignite.transactions.TransactionConcurrency;
import org.apache.ignite.transactions.TransactionIsolation;
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
public class IgniteProducerService implements ProducerService {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected ProducerConfigDao producerConfigDao;
    protected ProducerDao producerDao;

    @Inject
    public IgniteProducerService(ProducerDao igniteDao, ProducerConfigDao producerConfigDao) {
        this.producerConfigDao = producerConfigDao;
        this.producerDao = igniteDao;
    }

    @Inject
    protected Messenger messenger;

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
            return null;
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
    public Producer add(Producer producer) {
        Transaction tx = Ignition.ignite().transactions().tx();
        boolean commit = false;
        try {
            if (null == tx) {
                tx = Ignition.ignite().transactions().txStart(TransactionConcurrency.PESSIMISTIC, TransactionIsolation.READ_COMMITTED);
                commit = true;
            }
            producerDao.addOrUpdate(toIgniteModel(producer));
            if (null != producer.getProducerPolicy() || producer.getLimitPolicy() != null) {
                producerConfigDao.addOrUpdate(new IgniteProducerConfig(new IgniteProducer(producer)));
            }
            if (commit) {
                tx.commit();
            }
            this.publishEvent(ProducerEvent.add(producer.getTopic(), producer.getApp()));
            return producer;
        } catch (Exception e) {
            if (commit) tx.rollback();
            throw new RuntimeException("producerService addOrUpdate error", e);
        }
    }

    @Override
    public Producer update(Producer producer) {
        Transaction tx = Ignition.ignite().transactions().tx();
        boolean commit = false;
        try {
            if (null == tx) {
                tx = Ignition.ignite().transactions().txStart(TransactionConcurrency.PESSIMISTIC, TransactionIsolation.READ_COMMITTED);
                commit = true;
            }
            producerDao.addOrUpdate(toIgniteModel(producer));
            if (null != producer.getProducerPolicy() || producer.getLimitPolicy() != null) {
                producerConfigDao.addOrUpdate(new IgniteProducerConfig(new IgniteProducer(producer)));
            }
            if (commit) {
                tx.commit();
            }
            this.publishEvent(ProducerEvent.add(producer.getTopic(), producer.getApp()));
            return producer;
        } catch (Exception e) {
            if (commit) tx.rollback();
            throw new RuntimeException("producerService addOrUpdate error", e);
        }
    }

    public IgniteProducer toIgniteModel(Producer model) {
        return new IgniteProducer(model);
    }

    @Override
    public void delete(String id) {
        Producer producer = getById(id);
        Transaction tx = Ignition.ignite().transactions().tx();
        boolean commit = false;
        try {
            if (null == tx) {
                tx = Ignition.ignite().transactions().txStart(TransactionConcurrency.PESSIMISTIC, TransactionIsolation.READ_COMMITTED);
                commit = true;
            }
            producerDao.deleteById(id);
            producerConfigDao.deleteById(id);
            if (commit) tx.commit();
            this.publishEvent(ProducerEvent.remove(producer.getTopic(), producer.getApp()));
        } catch (Exception e) {
            if (commit) tx.rollback();
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
