package io.chubao.joyqueue.nsr.support;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.chubao.joyqueue.domain.Broker;
import io.chubao.joyqueue.domain.Consumer;
import io.chubao.joyqueue.domain.PartitionGroup;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.nsr.event.AddConsumerEvent;
import io.chubao.joyqueue.nsr.event.RemoveConsumerEvent;
import io.chubao.joyqueue.nsr.event.UpdateConsumerEvent;
import io.chubao.joyqueue.nsr.exception.NsrException;
import io.chubao.joyqueue.nsr.message.Messenger;
import io.chubao.joyqueue.nsr.service.ConsumerService;
import io.chubao.joyqueue.nsr.service.internal.BrokerInternalService;
import io.chubao.joyqueue.nsr.service.internal.ConsumerInternalService;
import io.chubao.joyqueue.nsr.service.internal.PartitionGroupInternalService;
import io.chubao.joyqueue.nsr.service.internal.TopicInternalService;
import io.chubao.joyqueue.nsr.service.internal.TransactionInternalService;
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
    private Messenger messenger;

    public DefaultConsumerService(TopicInternalService topicInternalService, PartitionGroupInternalService partitionGroupInternalService,
                                  BrokerInternalService brokerInternalService, ConsumerInternalService consumerInternalService,
                                  TransactionInternalService transactionInternalService, Messenger messenger) {
        this.topicInternalService = topicInternalService;
        this.partitionGroupInternalService = partitionGroupInternalService;
        this.brokerInternalService = brokerInternalService;
        this.consumerInternalService = consumerInternalService;
        this.transactionInternalService = transactionInternalService;
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
            throw new NsrException(String.format("topic: %s is not exist", consumer.getTopic()));
        }

        try {
            transactionInternalService.begin();
        } catch (Exception e) {
            logger.error("beginTransaction exception, topic: {}, app: {}", consumer.getTopic(), consumer.getApp(), e);
            throw new NsrException(e);
        }

        logger.info("addConsumer, topic: {}, app: {}", consumer.getTopic(), consumer.getApp());

        List<PartitionGroup> partitionGroups = partitionGroupInternalService.getByTopic(consumer.getTopic());
        List<Broker> replicas = getReplicas(partitionGroups);

        try {
            consumerInternalService.add(consumer);
            messenger.publish(new AddConsumerEvent(consumer.getTopic(), consumer), replicas);
            transactionInternalService.commit();
            return consumer;
        } catch (Exception e) {
            logger.error("addConsumer exception, topic: {}, app: {}", consumer.getTopic(), consumer.getApp(), e);
            messenger.fastPublish(new RemoveConsumerEvent(consumer.getTopic(), consumer), replicas);
            transactionInternalService.rollback();
            throw new NsrException(e);
        }
    }

    @Override
    public Consumer update(Consumer consumer) {
        Consumer oldConsumer = consumerInternalService.getByTopicAndApp(consumer.getTopic(), consumer.getApp());
        if (oldConsumer == null) {
            throw new NsrException(String.format("topic: %s, consumer: %s is not exist", oldConsumer.getTopic(), oldConsumer.getApp()));
        }

        try {
            transactionInternalService.begin();
        } catch (Exception e) {
            logger.error("beginTransaction exception, topic: {}, app: {}", consumer.getTopic(), consumer.getApp(), e);
            throw new NsrException(e);
        }

        logger.info("updateConsumer, topic: {}, app: {}", consumer.getTopic(), consumer.getApp());

        List<PartitionGroup> partitionGroups = partitionGroupInternalService.getByTopic(consumer.getTopic());
        List<Broker> replicas = getReplicas(partitionGroups);

        try {
            consumerInternalService.update(consumer);
            messenger.publish(new UpdateConsumerEvent(consumer.getTopic(), oldConsumer, consumer), replicas);
            transactionInternalService.commit();
            return consumer;
        } catch (Exception e) {
            logger.error("updateConsumer exception, topic: {}, app: {}", consumer.getTopic(), consumer.getApp(), e);
            messenger.fastPublish(new UpdateConsumerEvent(consumer.getTopic(), oldConsumer, consumer), replicas);
            transactionInternalService.rollback();
            throw new NsrException(e);
        }
    }

    @Override
    public void delete(String id) {
        Consumer consumer = consumerInternalService.getById(id);
        if (consumer == null) {
            throw new NsrException(String.format("consumer: %s is not exist", id));
        }

        try {
            transactionInternalService.begin();
        } catch (Exception e) {
            logger.error("beginTransaction exception, topic: {}, app: {}", consumer.getTopic(), consumer.getApp(), e);
            throw new NsrException(e);
        }

        logger.info("deleteConsumer, topic: {}, app: {}", consumer.getTopic(), consumer.getApp());

        List<PartitionGroup> partitionGroups = partitionGroupInternalService.getByTopic(consumer.getTopic());
        List<Broker> replicas = getReplicas(partitionGroups);

        try {
            consumerInternalService.delete(id);
            messenger.publish(new RemoveConsumerEvent(consumer.getTopic(), consumer), replicas);
            transactionInternalService.commit();
        } catch (Exception e) {
            logger.error("deleteConsumer exception, topic: {}, app: {}", consumer.getTopic(), consumer.getApp(), e);
            messenger.fastPublish(new AddConsumerEvent(consumer.getTopic(), consumer), replicas);
            transactionInternalService.rollback();
            throw new NsrException(e);
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