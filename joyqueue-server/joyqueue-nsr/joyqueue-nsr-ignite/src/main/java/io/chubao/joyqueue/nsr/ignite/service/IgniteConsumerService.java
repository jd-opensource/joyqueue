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
package io.chubao.joyqueue.nsr.ignite.service;

import com.google.inject.Inject;
import io.chubao.joyqueue.domain.Consumer;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.event.ConsumerEvent;
import io.chubao.joyqueue.event.MetaEvent;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.nsr.ignite.dao.ConsumerConfigDao;
import io.chubao.joyqueue.nsr.ignite.dao.ConsumerDao;
import io.chubao.joyqueue.nsr.ignite.model.IgniteBaseModel;
import io.chubao.joyqueue.nsr.ignite.model.IgniteConsumer;
import io.chubao.joyqueue.nsr.ignite.model.IgniteConsumerConfig;
import io.chubao.joyqueue.nsr.message.Messenger;
import io.chubao.joyqueue.nsr.model.ConsumerQuery;
import io.chubao.joyqueue.nsr.service.ConsumerService;
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
public class IgniteConsumerService implements ConsumerService {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected ConsumerConfigDao consumerConfigDao;
    protected ConsumerDao consumerDao;

    @Inject
    public IgniteConsumerService(ConsumerDao consumerDao, ConsumerConfigDao consumerConfigDao) {
        this.consumerDao = consumerDao;
        this.consumerConfigDao = consumerConfigDao;
    }

    @Inject
    protected Messenger messenger;

    @Override
    public Consumer getById(String id) {
        IgniteConsumer consumer = consumerDao.findById(id);
        if (null != consumer) {
            consumer.fillConfig(consumerConfigDao.findById(id));
        }
        return consumer;
    }

    @Override
    public Consumer get(Consumer model) {
        return this.getById(toIgniteModel(model).getId());
    }

    @Override
    public void deleteByTopicAndApp(TopicName topic, String app) {
        deleteById(IgniteConsumer.getId(topic, app));
    }

    public Consumer getByTopicAndApp(TopicName topic, String app) {
        return getById(IgniteConsumer.getId(topic, app));
    }

    public List<Consumer> getByTopic(TopicName topic, boolean withConfig) {
        ConsumerQuery consumerQuery = new ConsumerQuery(topic.getCode(), topic.getNamespace());
        List<IgniteConsumer> igniteConsumers = consumerDao.list(consumerQuery);
        if (null == igniteConsumers || igniteConsumers.size() < 1) return Collections.emptyList();
        Map<String, IgniteConsumerConfig> configs = new HashMap<>();
        if (withConfig) {
            List<IgniteConsumerConfig> igniteConsumerConfigs = consumerConfigDao.list(consumerQuery);
            if (null != igniteConsumerConfigs && igniteConsumerConfigs.size() > 0) {
                igniteConsumerConfigs.forEach(config -> {
                    configs.put(config.getId(), config);
                });
            }
        }
        List<Consumer> consumers = new ArrayList<>();
        igniteConsumers.forEach(consumer -> {
            consumers.add(consumer.fillConfig(configs.get(consumer.getId())));
        });
        return consumers;
    }

    public List<Consumer> getByApp(String app, boolean withConfig) {
        ConsumerQuery consumerQuery = new ConsumerQuery(app);
        List<IgniteConsumer> igniteConsumers = consumerDao.list(consumerQuery);
        if (null == igniteConsumers || igniteConsumers.size() < 1) return null;
        Map<String, IgniteConsumerConfig> configs = new HashMap<>();
        if (withConfig) {
            List<IgniteConsumerConfig> igniteConsumerConfigs = consumerConfigDao.list(consumerQuery);
            if (null != igniteConsumerConfigs && igniteConsumerConfigs.size() > 0) {
                igniteConsumerConfigs.forEach(config -> {
                    configs.put(config.getId(), config);
                });
            }
        }
        List<Consumer> consumers = new ArrayList<>();
        igniteConsumers.forEach(consumer -> {
            consumers.add(consumer.fillConfig(configs.get(consumer.getId())));
        });
        return consumers;
    }

    @Override
    public void addOrUpdate(Consumer consumer) {
        Transaction tx = Ignition.ignite().transactions().tx();
        boolean commit = false;
        try {
            if (null == tx) {
                tx = Ignition.ignite().transactions().txStart(TransactionConcurrency.PESSIMISTIC, TransactionIsolation.READ_COMMITTED);
                commit = true;
            }
            IgniteConsumer igConsumer = toIgniteModel(consumer);
            consumerDao.addOrUpdate(igConsumer);
            if (null != consumer.getConsumerPolicy() || null != consumer.getRetryPolicy() || consumer.getLimitPolicy() != null) {
                consumerConfigDao.addOrUpdate(new IgniteConsumerConfig(igConsumer));
            }
            if (commit) tx.commit();
        } catch (Exception e) {
            if (commit) tx.rollback();
            throw new RuntimeException("ConsumerService addOrUpdate error", e);
        } finally {
            if (commit) tx.close();
        }
    }


    public IgniteConsumer toIgniteModel(Consumer model) {
        return new IgniteConsumer(model);
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
            consumerDao.deleteById(id);
            consumerConfigDao.deleteById(String.valueOf(id));
            if (commit) tx.commit();
        } catch (Exception e) {
            if (commit) tx.rollback();
            throw new RuntimeException("ConsumerService deleteById error", e);
        } finally {
            if (commit) tx.close();
        }
    }

    @Override
    public void delete(Consumer model) {
        this.deleteById(toIgniteModel(model).getId());
    }

    @Override
    public List<Consumer> list() {
        return list(null);
    }

    @Override
    public List<Consumer> list(ConsumerQuery query) {
        return convert(consumerDao.list(query));
    }

    @Override
    public PageResult<Consumer> pageQuery(QPageQuery<ConsumerQuery> pageQuery) {
        PageResult<IgniteConsumer> pageResult = consumerDao.pageQuery(pageQuery);
        if (pageResult != null && pageResult.getResult() != null) {
            pageResult.getResult().stream().map(consumer -> {
                consumer.fillConfig(consumerConfigDao.findById(consumer.getId()));
                return consumer;
            }).collect(Collectors.toList());
        }return new PageResult<>(pageResult.getPagination(), convert(pageResult.getResult()));
    }

    public List<Consumer> getConsumerByClientType(byte clientType) {
        ConsumerQuery query = new ConsumerQuery();
        query.setClientType(clientType);
        return convert(consumerDao.list(query));
    }


    public void add(Consumer consumer) {
        try (Transaction tx = Ignition.ignite().transactions().txStart(TransactionConcurrency.PESSIMISTIC, TransactionIsolation.READ_COMMITTED)) {
            this.addOrUpdate(new IgniteConsumer(consumer));
            tx.commit();
            this.publishEvent(ConsumerEvent.add(consumer.getTopic(), consumer.getApp()));
        } catch (Exception e) {
            String message = String.format("add consumer [%s] ,topic [%s] error", consumer.getApp(), consumer.getTopic());
            logger.error(message, e);
            throw new RuntimeException(message, e);
        }
    }

    public void update(Consumer consumer) {
        try (Transaction tx = Ignition.ignite().transactions().txStart(TransactionConcurrency.PESSIMISTIC, TransactionIsolation.READ_COMMITTED)) {
            this.addOrUpdate(new IgniteConsumer(consumer));
            tx.commit();
            this.publishEvent(ConsumerEvent.update(consumer.getTopic(), consumer.getApp()));
        } catch (Exception e) {
            String message = String.format("update consumer [%s] ,topic [%s] error", consumer.getApp(), consumer.getTopic());
            logger.error(message, e);
            throw new RuntimeException(message, e);
        }
    }

    public void remove(Consumer consumer) {
        try (Transaction tx = Ignition.ignite().transactions().txStart(TransactionConcurrency.PESSIMISTIC, TransactionIsolation.READ_COMMITTED)) {
            this.deleteById(new StringBuffer(consumer.getTopic().getFullName()).append(IgniteBaseModel.SPLICE).append(consumer.getApp()).toString());
            tx.commit();
            this.publishEvent(ConsumerEvent.remove(consumer.getTopic(), consumer.getApp()));
        } catch (Exception e) {
            String message = String.format("remove consumer [%s] ,topic [%s] error", consumer.getApp(), consumer.getTopic());
            logger.error(message, e);
            throw new RuntimeException(message, e);
        }
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
