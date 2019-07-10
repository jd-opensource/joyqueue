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
package com.jd.joyqueue.nsr.ignite.service;

import com.google.inject.Inject;
import com.jd.joyqueue.domain.Producer;
import com.jd.joyqueue.domain.TopicName;
import com.jd.joyqueue.event.MetaEvent;
import com.jd.joyqueue.event.ProducerEvent;
import com.jd.joyqueue.model.PageResult;
import com.jd.joyqueue.model.QPageQuery;
import com.jd.joyqueue.nsr.ignite.dao.ProducerConfigDao;
import com.jd.joyqueue.nsr.ignite.dao.ProducerDao;
import com.jd.joyqueue.nsr.ignite.model.IgniteProducer;
import com.jd.joyqueue.nsr.ignite.model.IgniteProducerConfig;
import com.jd.joyqueue.nsr.message.Messenger;
import com.jd.joyqueue.nsr.model.ProducerQuery;
import com.jd.joyqueue.nsr.service.ProducerService;
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
import java.util.stream.Collectors;

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
    public void deleteByTopicAndApp(TopicName topic, String app) {
        deleteById(IgniteProducer.getId(topic, app));
    }

    @Override
    public Producer getByTopicAndApp(TopicName topic, String app) {
        return getById(IgniteProducer.getId(topic, app));
    }

    @Override
    public List<Producer> getByTopic(TopicName topic, boolean withConfig) { ProducerQuery producerQuery = new ProducerQuery(topic.getCode(), topic.getNamespace());
        List<IgniteProducer> igniteProducers = producerDao.list(producerQuery);
        if (null == igniteProducers || igniteProducers.size() < 1) {
            return Collections.emptyList();
        }
        Map<String, IgniteProducerConfig> configs = new HashMap<>();
        if (withConfig) {
            List<IgniteProducerConfig> igniteProducerConfigs = producerConfigDao.list(producerQuery);
            if (null != igniteProducerConfigs && igniteProducerConfigs.size() > 0) {
                igniteProducerConfigs.forEach(config -> {
                    configs.put(config.getId(), config);
                });
            }
        }
        List<Producer> producers = new ArrayList<>();
        igniteProducers.forEach(producer -> {
            producers.add(producer.fillConfig(configs.get(producer.getId())));
        });
        return producers;
    }

    public List<Producer> getByApp(String app, boolean withConfig) {
        ProducerQuery producerQuery = new ProducerQuery(app);
        List<IgniteProducer> igniteProducers = producerDao.list(producerQuery);
        if (null == igniteProducers || igniteProducers.size() < 1) {
            return null;
        }
        Map<String, IgniteProducerConfig> configs = new HashMap<>();
        if (withConfig) {
            List<IgniteProducerConfig> igniteProducerConfigs = producerConfigDao.list(producerQuery);
            if (null != igniteProducerConfigs && igniteProducerConfigs.size() > 0) {
                igniteProducerConfigs.forEach(config -> {
                    configs.put(config.getId(), config);
                });
            }
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
    public Producer get(Producer model) {
        return producerDao.findById(toIgniteModel(model).getId());
    }

    @Override
    public void addOrUpdate(Producer producer) {
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
        } catch (Exception e) {
            if (commit) tx.rollback();
            throw new RuntimeException("producerService addOrUpdate error", e);
        }
    }

    public IgniteProducer toIgniteModel(Producer model) {
        return new IgniteProducer(model);
    }

    @Override
    public void deleteById(String id) {
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
        } catch (Exception e) {
            if (commit) tx.rollback();
            throw new RuntimeException("delete producer error.", e);
        }
    }

    @Override
    public List<Producer> list() {
        return convert(producerDao.list(null));
    }

    @Override
    public PageResult<Producer> pageQuery(QPageQuery pageQuery) {
        PageResult<IgniteProducer> pageResult = producerDao.pageQuery(pageQuery);
        if (pageResult != null && pageResult.getResult() != null) {
            pageResult.getResult().stream().map(producer -> {
                producer.fillConfig(producerConfigDao.findById(producer.getId()));
                return producer;
            }).collect(Collectors.toList());
        }
        return new PageResult(pageResult.getPagination(),pageResult.getResult());
    }

    @Override
    public List<Producer> list(ProducerQuery query) {
        return convert(producerDao.list(query));
    }

    @Override
    public void delete(Producer model) {
        producerDao.deleteById(toIgniteModel(model).getId());
    }

    public void add(Producer producer) {
        Transaction tx = Ignition.ignite().transactions().txStart(TransactionConcurrency.PESSIMISTIC, TransactionIsolation.READ_COMMITTED);
        try {
            this.addOrUpdate(new IgniteProducer(producer));
            tx.commit();
            this.publishEvent(ProducerEvent.add(producer.getTopic(), producer.getApp()));
        } catch (Exception e) {
            logger.error("add producer [{}] ,topic [{}] error", producer.getApp(), producer.getTopic(), e);
            tx.rollback();
        }
    }

    public void update(Producer producer) {
        Transaction tx = Ignition.ignite().transactions().txStart(TransactionConcurrency.PESSIMISTIC, TransactionIsolation.READ_COMMITTED);
        try {
            this.addOrUpdate(new IgniteProducer(producer));
            tx.commit();
            this.publishEvent(ProducerEvent.update(producer.getTopic(), producer.getApp()));
        } catch (Exception e) {
            tx.rollback();
            logger.error("update producer [{}] ,topic [{}] error", producer.getApp(), producer.getTopic(), e);
        }
    }

    public void remove(Producer producer) {
        Transaction tx = Ignition.ignite().transactions().txStart(TransactionConcurrency.PESSIMISTIC, TransactionIsolation.READ_COMMITTED);
        try {
            this.delete(producer);
            tx.commit();
            this.publishEvent(ProducerEvent.remove(producer.getTopic(), producer.getApp()));
        } catch (Exception e) {
            tx.rollback();
            logger.error("remove producer [{}] ,topic [{}] error", producer.getApp(), producer.getTopic(), e);

        }
    }

    public void publishEvent(MetaEvent event) {
        messenger.publish(event);
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
