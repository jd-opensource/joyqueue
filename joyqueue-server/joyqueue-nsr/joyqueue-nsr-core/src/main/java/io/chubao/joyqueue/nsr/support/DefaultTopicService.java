package io.chubao.joyqueue.nsr.support;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.chubao.joyqueue.domain.Broker;
import io.chubao.joyqueue.domain.PartitionGroup;
import io.chubao.joyqueue.domain.Topic;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.model.QPageQuery;
import io.chubao.joyqueue.nsr.event.AddPartitionGroupEvent;
import io.chubao.joyqueue.nsr.event.AddTopicEvent;
import io.chubao.joyqueue.nsr.event.LeaderChangeEvent;
import io.chubao.joyqueue.nsr.event.RemovePartitionGroupEvent;
import io.chubao.joyqueue.nsr.event.RemoveTopicEvent;
import io.chubao.joyqueue.nsr.event.UpdatePartitionGroupEvent;
import io.chubao.joyqueue.nsr.event.UpdateTopicEvent;
import io.chubao.joyqueue.nsr.exception.NsrException;
import io.chubao.joyqueue.nsr.message.Messenger;
import io.chubao.joyqueue.nsr.model.TopicQuery;
import io.chubao.joyqueue.nsr.service.TopicService;
import io.chubao.joyqueue.nsr.service.internal.BrokerInternalService;
import io.chubao.joyqueue.nsr.service.internal.PartitionGroupInternalService;
import io.chubao.joyqueue.nsr.service.internal.TopicInternalService;
import io.chubao.joyqueue.nsr.service.internal.TransactionInternalService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * DefaultTopicService
 * author: gaohaoxiang
 * date: 2019/8/27
 */
public class DefaultTopicService implements TopicService {

    protected static final Logger logger = LoggerFactory.getLogger(DefaultTopicService.class);

    private Messenger messenger;
    private TopicInternalService topicInternalService;
    private PartitionGroupInternalService partitionGroupInternalService;
    private BrokerInternalService brokerInternalService;
    private TransactionInternalService transactionInternalService;

    public DefaultTopicService(Messenger messenger, TopicInternalService topicInternalService, PartitionGroupInternalService partitionGroupInternalService,
                               BrokerInternalService brokerInternalService, TransactionInternalService transactionInternalService) {
        this.messenger = messenger;
        this.topicInternalService = topicInternalService;
        this.partitionGroupInternalService = partitionGroupInternalService;
        this.brokerInternalService = brokerInternalService;
        this.transactionInternalService = transactionInternalService;
    }

    @Override
    public Topic getById(String id) {
        return topicInternalService.getById(id);
    }

    @Override
    public Topic getTopicByCode(String namespace, String topic) {
        return topicInternalService.getTopicByCode(namespace, topic);
    }

    @Override
    public PageResult<Topic> search(QPageQuery<TopicQuery> pageQuery) {
        return topicInternalService.search(pageQuery);
    }

    @Override
    public PageResult<Topic> findUnsubscribedByQuery(QPageQuery<TopicQuery> pageQuery) {
        return topicInternalService.findUnsubscribedByQuery(pageQuery);
    }

    @Override
    public List<Topic> getAll() {
        return topicInternalService.getAll();
    }

    @Override
    public void addTopic(Topic topic, List<PartitionGroup> partitionGroups) {
        if (topicInternalService.getTopicByCode(topic.getName().getNamespace(), topic.getName().getCode()) != null) {
            throw new NsrException(String.format("topic: %s is already exist", topic.getName()));
        }

        try {
            transactionInternalService.begin();
        } catch (Exception e) {
            logger.error("beginTransaction exception, topic: {}, partitionGroups: {}", topic, partitionGroups, e);
            throw new NsrException(e);
        }

        logger.info("addTopic, topic: {}, partitionGroups: {}", topic, partitionGroups);

        fillBroker(partitionGroups);
        List<Broker> replicas = getReplicas(partitionGroups);

        try {
            topicInternalService.addTopic(topic, partitionGroups);
            messenger.publish(new AddTopicEvent(topic, partitionGroups), replicas);
            transactionInternalService.commit();
        } catch (Exception e) {
            logger.error("addTopic exception, topic: {}, partitionGroups: {}", topic, partitionGroups, e);
            messenger.fastPublish(new RemoveTopicEvent(topic, partitionGroups), replicas);
            transactionInternalService.rollback();
            throw new NsrException(e);
        }
    }

    @Override
    public void removeTopic(Topic topic) {
        topic = topicInternalService.getTopicByCode(topic.getName().getNamespace(), topic.getName().getCode());
        if (topic == null) {
            throw new NsrException(String.format("topic: %s is not exist", topic.getName()));
        }

        try {
            transactionInternalService.begin();
        } catch (Exception e) {
            logger.error("beginTransaction exception, topic: {}", topic, e);
            throw new NsrException(e);
        }

        logger.info("removeTopic, topic: {}", topic);

        List<PartitionGroup> partitionGroups = partitionGroupInternalService.getByTopic(topic.getName());
        fillBroker(partitionGroups);
        List<Broker> replicas = getReplicas(partitionGroups);

        try {
            topicInternalService.removeTopic(topic);
            messenger.publish(new RemoveTopicEvent(topic, partitionGroups), replicas);
            transactionInternalService.commit();
        } catch (Exception e) {
            logger.error("removeTopic exception, topic: {}, partitionGroups: {}", topic, partitionGroups, e);
            messenger.fastPublish(new AddTopicEvent(topic, partitionGroups), replicas);
            transactionInternalService.rollback();
            throw new NsrException(e);
        }
    }

    @Override
    public void addPartitionGroup(PartitionGroup partitionGroup) {
        if (topicInternalService.getTopicByCode(partitionGroup.getTopic().getNamespace(), partitionGroup.getTopic().getCode()) == null) {
            throw new NsrException(String.format("topic: %s is not exist", partitionGroup.getTopic()));
        }

        try {
            transactionInternalService.begin();
        } catch (Exception e) {
            logger.error("beginTransaction exception, topic: {}, partitionGroup: {}", partitionGroup.getTopic(), partitionGroup, e);
            throw new NsrException(e);
        }

        logger.info("addPartitionGroup, topic: {}, partitionGroup: {}", partitionGroup.getTopic(), partitionGroup);

        fillBroker(partitionGroup);
        List<Broker> replicas = getReplicas(partitionGroup);

        try {
            if (PartitionGroup.ElectType.fix.equals(partitionGroup.getElectType())) {
                partitionGroup.setLeader(partitionGroup.getReplicas().iterator().next());
            }

            topicInternalService.addPartitionGroup(partitionGroup);
            messenger.publish(new AddPartitionGroupEvent(partitionGroup.getTopic(), partitionGroup), replicas);
            transactionInternalService.commit();
        } catch (Exception e) {
            logger.error("addPartitionGroup exception, topic: {}, partitionGroup: {}", partitionGroup.getTopic(), partitionGroup.getGroup(), e);
            messenger.fastPublish(new RemovePartitionGroupEvent(partitionGroup.getTopic(), partitionGroup), replicas);
            transactionInternalService.rollback();
            throw new NsrException(e);
        }
    }

    @Override
    public void removePartitionGroup(PartitionGroup partitionGroup) {
        if (topicInternalService.getTopicByCode(partitionGroup.getTopic().getNamespace(), partitionGroup.getTopic().getCode()) == null) {
            throw new NsrException(String.format("topic: %s is not exist", partitionGroup.getTopic()));
        }

        PartitionGroup oldPartitionGroup = partitionGroupInternalService.getByTopicAndGroup(partitionGroup.getTopic(), partitionGroup.getGroup());
        if (oldPartitionGroup == null) {
            throw new NsrException(String.format("topic: %s, group: %s is not exist", partitionGroup.getTopic(), partitionGroup.getGroup()));
        }

        try {
            transactionInternalService.begin();
        } catch (Exception e) {
            logger.error("beginTransaction exception, topic: {}, partitionGroup: {}", partitionGroup.getTopic(), partitionGroup, e);
            throw new NsrException(e);
        }

        logger.info("removePartitionGroup, topic: {}, partitionGroup: {}", partitionGroup.getTopic(), partitionGroup);

        fillBroker(oldPartitionGroup);
        List<Broker> replicas = getReplicas(oldPartitionGroup);

        try {
            topicInternalService.removePartitionGroup(partitionGroup);
            messenger.publish(new RemovePartitionGroupEvent(partitionGroup.getTopic(), oldPartitionGroup), replicas);
            transactionInternalService.commit();
        } catch (Exception e) {
            logger.error("removePartitionGroup exception, topic: {}, partitionGroup: {}", partitionGroup.getTopic(), partitionGroup.getGroup(), e);
            messenger.fastPublish(new AddPartitionGroupEvent(partitionGroup.getTopic(), oldPartitionGroup), replicas);
            transactionInternalService.rollback();
            throw new NsrException(e);
        }
    }

    @Override
    public Collection<Integer> updatePartitionGroup(PartitionGroup partitionGroup) {
        Topic oldTopic = topicInternalService.getTopicByCode(partitionGroup.getTopic().getNamespace(), partitionGroup.getTopic().getCode());
        if (oldTopic == null) {
            throw new NsrException(String.format("topic: %s is not exist", partitionGroup.getTopic()));
        }

        PartitionGroup oldPartitionGroup = partitionGroupInternalService.getByTopicAndGroup(partitionGroup.getTopic(), partitionGroup.getGroup());
        if (oldPartitionGroup == null) {
            throw new NsrException(String.format("topic: %s, group: %s is not exist", partitionGroup.getTopic(), partitionGroup.getGroup()));
        }

        try {
            transactionInternalService.begin();
        } catch (Exception e) {
            logger.error("beginTransaction exception, topic: {}, partitionGroup: {}", partitionGroup.getTopic(), partitionGroup, e);
            throw new NsrException(e);
        }

        logger.info("updatePartitionGroup, topic: {}, partitionGroup: {}", partitionGroup.getTopic(), partitionGroup);

        fillBroker(partitionGroup);
        fillBroker(oldPartitionGroup);
        List<Broker> oldReplicas = getReplicas(oldPartitionGroup);
        List<Broker> newReplicas = getReplicas(partitionGroup);
        Set<Broker> replicas = Sets.newHashSet();

        replicas.addAll(oldReplicas);
        replicas.addAll(newReplicas);

        if (CollectionUtils.isEmpty(partitionGroup.getReplicas())) {
            partitionGroup.setLeader(-1);
            partitionGroup.setTerm(0);
        }

        try {
            topicInternalService.updatePartitionGroup(partitionGroup);
            messenger.publish(new UpdatePartitionGroupEvent(partitionGroup.getTopic(), oldPartitionGroup, partitionGroup), Lists.newArrayList(replicas));
            transactionInternalService.commit();
            return Collections.emptyList();
        } catch (Exception e) {
            logger.error("updatePartitionGroup exception, topic: {}, partitionGroup: {}", partitionGroup.getTopic(), partitionGroup.getGroup(), e);
            messenger.fastPublish(new UpdatePartitionGroupEvent(partitionGroup.getTopic(), partitionGroup, oldPartitionGroup), Lists.newArrayList(replicas));
            transactionInternalService.rollback();
            throw new NsrException(e);
        }
    }

    @Override
    public void leaderReport(PartitionGroup group) {
        PartitionGroup oldPartitionGroup = partitionGroupInternalService.getById(group.getId());
        if (oldPartitionGroup == null) {
            throw new NsrException(String.format("topic: %s, group: %s is not exist", oldPartitionGroup.getTopic(), oldPartitionGroup.getGroup()));
        }

        try {
            transactionInternalService.begin();
        } catch (Exception e) {
            logger.error("beginTransaction exception, topic: {}, partitionGroup: {}", group.getTopic(), group, e);
            throw new NsrException(e);
        }

        PartitionGroup newPartitionGroup = oldPartitionGroup.clone();
        newPartitionGroup.setIsrs(group.getIsrs());
        newPartitionGroup.setLeader(group.getLeader());
        newPartitionGroup.setTerm(group.getTerm());

        fillBroker(newPartitionGroup);
        List<Broker> replicas = getReplicas(newPartitionGroup);

        try {
            topicInternalService.leaderReport(newPartitionGroup);
            messenger.publish(new UpdatePartitionGroupEvent(oldPartitionGroup.getTopic(), oldPartitionGroup, newPartitionGroup), replicas);
            transactionInternalService.commit();
        } catch (Exception e) {
            logger.error("leader report exception, topic: {}, partitionGroup: {}", newPartitionGroup.getTopic(), newPartitionGroup.getGroup(), e);
            messenger.fastPublish(new UpdatePartitionGroupEvent(newPartitionGroup.getTopic(), newPartitionGroup, oldPartitionGroup), replicas);
            transactionInternalService.rollback();
            throw new NsrException(e);
        }
    }

    @Override
    public void leaderChange(PartitionGroup group) {
        if (topicInternalService.getTopicByCode(group.getTopic().getNamespace(), group.getTopic().getCode()) == null) {
            throw new NsrException(String.format("topic: %s is not exist", group.getTopic()));
        }

        PartitionGroup oldPartitionGroup = partitionGroupInternalService.getByTopicAndGroup(group.getTopic(), group.getGroup());
        if (oldPartitionGroup == null) {
            throw new NsrException(String.format("topic: %s, group: %s is not exist", group.getTopic(), group.getGroup()));
        }

        Broker leader = brokerInternalService.getById(group.getLeader());
        if (leader == null) {
            throw new NsrException(String.format("topic: %s, group: %s, broker: {} is not exist", group.getTopic(), group.getGroup(), group.getLeader()));
        }

        try {
            transactionInternalService.begin();
        } catch (Exception e) {
            logger.error("beginTransaction exception, topic: {}, partitionGroup: {}", group.getTopic(), group, e);
            throw new NsrException(e);
        }

        logger.info("leaderChange, topic: {}, partitionGroup: {}", group.getTopic(), group);
        Broker oldLeader = brokerInternalService.getById(oldPartitionGroup.getLeader());

        try {
            topicInternalService.leaderChange(group);
            messenger.publish(new LeaderChangeEvent(group.getTopic(), oldPartitionGroup, group), leader);
            transactionInternalService.commit();
        } catch (Exception e) {
            logger.error("leaderChange exception, topic: {}, partitionGroup: {}", group.getTopic(), group.getGroup(), e);
            if (oldLeader != null) {
                messenger.fastPublish(new LeaderChangeEvent(group.getTopic(), group, oldPartitionGroup), oldLeader);
            }
            transactionInternalService.rollback();
            throw new NsrException(e);
        }
    }

    @Override
    public List<PartitionGroup> getPartitionGroup(String namespace, String topic, Object[] groups) {
        return topicInternalService.getPartitionGroup(namespace, topic, groups);
    }

    @Override
    public Topic update(Topic topic) {
        Topic oldTopic = topicInternalService.getTopicByCode(topic.getName().getNamespace(), topic.getName().getCode());
        if (oldTopic == null) {
            throw new NsrException(String.format("topic: %s is not exist", topic.getName()));
        }

        try {
            transactionInternalService.begin();
        } catch (Exception e) {
            logger.error("beginTransaction exception, topic: {}", topic, e);
            throw new NsrException(e);
        }

        logger.info("update, topic: {}", topic);

        List<PartitionGroup> partitionGroups = partitionGroupInternalService.getByTopic(topic.getName());
        List<Broker> replicas = getReplicas(partitionGroups);
        try {
            topicInternalService.update(topic);
            messenger.publish(new UpdateTopicEvent(oldTopic, topic), replicas);
            transactionInternalService.commit();
            return topic;
        } catch (Exception e) {
            logger.error("removeTopic exception, topic: {}, partitionGroups: {}", topic, partitionGroups, e);
            messenger.publish(new UpdateTopicEvent(topic, oldTopic), replicas);
            transactionInternalService.rollback();
            throw new NsrException(e);
        }
    }

    protected void fillBroker(PartitionGroup partitionGroup) {
        Set<Integer> replicaIds = Sets.newHashSet();
        if (partitionGroup.getReplicas() != null) {
            replicaIds.addAll(partitionGroup.getReplicas());
        }
        if (partitionGroup.getLearners() != null) {
            replicaIds.addAll(partitionGroup.getLearners());
        }

        List<Broker> brokers = brokerInternalService.getByIds(Lists.newArrayList(replicaIds));
        Map<Integer, Broker> brokerMap = Maps.newHashMap();
        for (Broker broker : brokers) {
            brokerMap.put(broker.getId(), broker);
        }

        partitionGroup.setBrokers(brokerMap);
    }

    protected void fillBroker(List<PartitionGroup> partitionGroups) {
        if (CollectionUtils.isEmpty(partitionGroups)) {
            return;
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

        List<Broker> brokers = brokerInternalService.getByIds(Lists.newArrayList(replicaIds));
        Map<Integer, Broker> brokersMap = Maps.newHashMap();
        for (Broker broker : brokers) {
            brokersMap.put(broker.getId(), broker);
        }

        for (PartitionGroup partitionGroup : partitionGroups) {
            Map<Integer, Broker> brokerMap = Maps.newHashMap();
            if (partitionGroup.getReplicas() != null) {
                for (Integer replica : partitionGroup.getReplicas()) {
                    Broker broker = brokersMap.get(replica);
                    if (broker != null) {
                        brokerMap.put(replica, broker);
                    }
                }
            }
            if (partitionGroup.getLearners() != null) {
                for (Integer learners : partitionGroup.getLearners()) {
                    Broker broker = brokersMap.get(learners);
                    if (broker != null) {
                        brokerMap.put(learners, broker);
                    }
                }
            }
            partitionGroup.setBrokers(brokerMap);
        }
    }

    protected List<Broker> getReplicas(PartitionGroup partitionGroup) {
        return Lists.newArrayList(partitionGroup.getBrokers().values());
    }

    protected List<Broker> getReplicas(List<PartitionGroup> partitionGroups) {
        Set<Broker> result = Sets.newHashSet();
        for (PartitionGroup partitionGroup : partitionGroups) {
            result.addAll(partitionGroup.getBrokers().values());
        }
        return Lists.newArrayList(result);
    }
}
