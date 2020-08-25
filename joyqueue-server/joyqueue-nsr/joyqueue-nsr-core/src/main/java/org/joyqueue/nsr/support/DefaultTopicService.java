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
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.joyqueue.domain.Broker;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.Topic;
import org.joyqueue.model.PageResult;
import org.joyqueue.model.QPageQuery;
import org.joyqueue.nsr.config.NameServiceConfig;
import org.joyqueue.nsr.event.AddPartitionGroupEvent;
import org.joyqueue.nsr.event.AddTopicEvent;
import org.joyqueue.nsr.event.LeaderChangeEvent;
import org.joyqueue.nsr.event.RemovePartitionGroupEvent;
import org.joyqueue.nsr.event.RemoveTopicEvent;
import org.joyqueue.nsr.event.UpdatePartitionGroupEvent;
import org.joyqueue.nsr.event.UpdateTopicEvent;
import org.joyqueue.nsr.exception.NsrException;
import org.joyqueue.nsr.message.Messenger;
import org.joyqueue.nsr.model.TopicQuery;
import org.joyqueue.nsr.service.TopicService;
import org.joyqueue.nsr.service.internal.BrokerInternalService;
import org.joyqueue.nsr.service.internal.PartitionGroupInternalService;
import org.joyqueue.nsr.service.internal.TopicInternalService;
import org.joyqueue.nsr.service.internal.TransactionInternalService;
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

    private NameServiceConfig config;
    private Messenger messenger;
    private TopicInternalService topicInternalService;
    private PartitionGroupInternalService partitionGroupInternalService;
    private BrokerInternalService brokerInternalService;
    private TransactionInternalService transactionInternalService;

    public DefaultTopicService(NameServiceConfig config, Messenger messenger, TopicInternalService topicInternalService, PartitionGroupInternalService partitionGroupInternalService,
                               BrokerInternalService brokerInternalService, TransactionInternalService transactionInternalService) {
        this.config = config;
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

        logger.info("addTopic, topic: {}, partitionGroups: {}", topic.getName(), partitionGroups);

        fillBroker(partitionGroups);
        List<Broker> replicas = getReplicas(partitionGroups);

        for (PartitionGroup partitionGroup : partitionGroups) {
            if (PartitionGroup.ElectType.fix.equals(partitionGroup.getElectType())) {
                partitionGroup.setLeader(partitionGroup.getReplicas().iterator().next());
            }
        }

        try {
            transactionInternalService.begin();
        } catch (Exception e) {
            logger.error("beginTransaction exception, topic: {}, partitionGroups: {}", topic, partitionGroups, e);
            throw new NsrException(e);
        }

        try {
            topicInternalService.addTopic(topic, partitionGroups);
            transactionInternalService.commit();
        } catch (Exception e) {
            logger.error("addTopic exception, topic: {}, partitionGroups: {}", topic, partitionGroups, e);
            transactionInternalService.rollback();
            throw new NsrException(e);
        }

        if (config.getMessengerPublishTopicEnable()) {
            messenger.publish(new AddTopicEvent(topic, partitionGroups), replicas);
        }
    }

    @Override
    public void removeTopic(Topic topic) {
        topic = topicInternalService.getTopicByCode(topic.getName().getNamespace(), topic.getName().getCode());
        if (topic == null) {
            throw new NsrException(String.format("topic: %s does not exist", topic.getName()));
        }

        logger.info("removeTopic, topic: {}", topic);

        List<PartitionGroup> partitionGroups = partitionGroupInternalService.getByTopic(topic.getName());
        fillBroker(partitionGroups);
        List<Broker> replicas = getReplicas(partitionGroups);

        try {
            transactionInternalService.begin();
        } catch (Exception e) {
            logger.error("beginTransaction exception, topic: {}", topic, e);
            throw new NsrException(e);
        }

        try {
            topicInternalService.removeTopic(topic);
            transactionInternalService.commit();
        } catch (Exception e) {
            logger.error("removeTopic exception, topic: {}, partitionGroups: {}", topic, partitionGroups, e);
            transactionInternalService.rollback();
            throw new NsrException(e);
        }

        if (config.getMessengerPublishTopicEnable()) {
            messenger.publish(new RemoveTopicEvent(topic, partitionGroups), replicas);
        }
    }

    @Override
    public void addPartitionGroup(PartitionGroup partitionGroup) {
        if (topicInternalService.getTopicByCode(partitionGroup.getTopic().getNamespace(), partitionGroup.getTopic().getCode()) == null) {
            throw new NsrException(String.format("topic: %s does not exist", partitionGroup.getTopic()));
        }
        if (partitionGroupInternalService.getByTopicAndGroup(partitionGroup.getTopic(), partitionGroup.getGroup()) != null) {
            throw new NsrException(String.format("topic: %s, group: %s does not exist", partitionGroup.getTopic(), partitionGroup.getGroup()));
        }

        logger.info("addPartitionGroup, topic: {}, partitionGroup: {}", partitionGroup.getTopic(), partitionGroup);

        fillBroker(partitionGroup);
        List<Broker> replicas = getReplicas(partitionGroup);

        if (PartitionGroup.ElectType.fix.equals(partitionGroup.getElectType())) {
            partitionGroup.setLeader(partitionGroup.getReplicas().iterator().next());
        }

        try {
            transactionInternalService.begin();
        } catch (Exception e) {
            logger.error("beginTransaction exception, topic: {}, partitionGroup: {}", partitionGroup.getTopic(), partitionGroup, e);
            throw new NsrException(e);
        }

        try {
            topicInternalService.addPartitionGroup(partitionGroup);
            transactionInternalService.commit();
        } catch (Exception e) {
            logger.error("addPartitionGroup exception, topic: {}, partitionGroup: {}", partitionGroup.getTopic(), partitionGroup.getGroup(), e);
            transactionInternalService.rollback();
            throw new NsrException(e);
        }

        if (config.getMessengerPublishTopicEnable()) {
            messenger.publish(new AddPartitionGroupEvent(partitionGroup.getTopic(), partitionGroup), replicas);
        }
    }

    @Override
    public void removePartitionGroup(PartitionGroup partitionGroup) {
        if (topicInternalService.getTopicByCode(partitionGroup.getTopic().getNamespace(), partitionGroup.getTopic().getCode()) == null) {
            throw new NsrException(String.format("topic: %s does not exist", partitionGroup.getTopic()));
        }

        PartitionGroup oldPartitionGroup = partitionGroupInternalService.getByTopicAndGroup(partitionGroup.getTopic(), partitionGroup.getGroup());
        if (oldPartitionGroup == null) {
            throw new NsrException(String.format("topic: %s, group: %s does not exist", partitionGroup.getTopic(), partitionGroup.getGroup()));
        }

        logger.info("removePartitionGroup, topic: {}, partitionGroup: {}", partitionGroup.getTopic(), partitionGroup);

        fillBroker(oldPartitionGroup);
        List<Broker> replicas = getReplicas(oldPartitionGroup);

        try {
            transactionInternalService.begin();
        } catch (Exception e) {
            logger.error("beginTransaction exception, topic: {}, partitionGroup: {}", partitionGroup.getTopic(), partitionGroup, e);
            throw new NsrException(e);
        }

        try {
            topicInternalService.removePartitionGroup(partitionGroup);
            transactionInternalService.commit();
        } catch (Exception e) {
            logger.error("removePartitionGroup exception, topic: {}, partitionGroup: {}", partitionGroup.getTopic(), partitionGroup.getGroup(), e);
            transactionInternalService.rollback();
            throw new NsrException(e);
        }

        if (config.getMessengerPublishTopicEnable()) {
            messenger.publish(new RemovePartitionGroupEvent(partitionGroup.getTopic(), oldPartitionGroup), replicas);
        }
    }

    @Override
    public Collection<Integer> updatePartitionGroup(PartitionGroup partitionGroup) {
        Topic oldTopic = topicInternalService.getTopicByCode(partitionGroup.getTopic().getNamespace(), partitionGroup.getTopic().getCode());
        if (oldTopic == null) {
            throw new NsrException(String.format("topic: %s does not exist", partitionGroup.getTopic()));
        }

        PartitionGroup oldPartitionGroup = partitionGroupInternalService.getByTopicAndGroup(partitionGroup.getTopic(), partitionGroup.getGroup());
        if (oldPartitionGroup == null) {
            throw new NsrException(String.format("topic: %s, group: %s is exist", partitionGroup.getTopic(), partitionGroup.getGroup()));
        }

        logger.info("updatePartitionGroup, topic: {}, partitionGroup: {}", partitionGroup.getTopic(), partitionGroup);

        fillBroker(partitionGroup);
        fillBroker(oldPartitionGroup);
        List<Broker> oldReplicas = getReplicas(oldPartitionGroup);
        List<Broker> newReplicas = getReplicas(partitionGroup);
        Set<Broker> replicas = Sets.newHashSet();

        replicas.addAll(oldReplicas);
        replicas.addAll(newReplicas);

        partitionGroup.setLeader(oldPartitionGroup.getLeader());
        partitionGroup.setIsrs(oldPartitionGroup.getIsrs());
        partitionGroup.setTerm(oldPartitionGroup.getTerm());

        if (CollectionUtils.isEmpty(partitionGroup.getReplicas())) {
            partitionGroup.setLeader(-1);
            partitionGroup.setTerm(0);
        }

        try {
            transactionInternalService.begin();
        } catch (Exception e) {
            logger.error("beginTransaction exception, topic: {}, partitionGroup: {}", partitionGroup.getTopic(), partitionGroup, e);
            throw new NsrException(e);
        }

        try {
            topicInternalService.updatePartitionGroup(partitionGroup);
            transactionInternalService.commit();
        } catch (Exception e) {
            logger.error("updatePartitionGroup exception, topic: {}, partitionGroup: {}", partitionGroup.getTopic(), partitionGroup.getGroup(), e);
            transactionInternalService.rollback();
            throw new NsrException(e);
        }

        if (config.getMessengerPublishTopicEnable()) {
            messenger.publish(new UpdatePartitionGroupEvent(partitionGroup.getTopic(), oldPartitionGroup, partitionGroup), Lists.newArrayList(replicas));
        }
        return Collections.emptyList();
    }

    @Override
    public void leaderReport(PartitionGroup group) {
        PartitionGroup oldPartitionGroup = partitionGroupInternalService.getById(group.getId());
        if (oldPartitionGroup == null) {
            throw new NsrException(String.format("topic: %s, group: %s does not exist", oldPartitionGroup.getTopic(), oldPartitionGroup.getGroup()));
        }

        if (oldPartitionGroup.getLeader().equals(group.getLeader()) && oldPartitionGroup.getTerm().equals(group.getTerm())) {
            return;
        }

        PartitionGroup newPartitionGroup = oldPartitionGroup.clone();
        newPartitionGroup.setIsrs(group.getIsrs());
        newPartitionGroup.setLeader(group.getLeader());
        newPartitionGroup.setTerm(group.getTerm());

        fillBroker(newPartitionGroup);
        List<Broker> replicas = getReplicas(newPartitionGroup);

        logger.info("leader report, topic: {}, partitionGroup: {}", group.getTopic(), group.getGroup());

        try {
            transactionInternalService.begin();
        } catch (Exception e) {
            logger.error("beginTransaction exception, topic: {}, partitionGroup: {}", group.getTopic(), group, e);
            throw new NsrException(e);
        }

        try {
            topicInternalService.leaderReport(newPartitionGroup);
            transactionInternalService.commit();
        } catch (Exception e) {
            logger.error("leader report exception, topic: {}, partitionGroup: {}", newPartitionGroup.getTopic(), newPartitionGroup.getGroup(), e);
            transactionInternalService.rollback();
            throw new NsrException(e);
        }

        if (config.getMessengerPublishLeaderReportEnable()) {
            messenger.publish(new UpdatePartitionGroupEvent(oldPartitionGroup.getTopic(), oldPartitionGroup, newPartitionGroup), replicas);
        }
    }

    @Override
    public void leaderChange(PartitionGroup group) {
        if (topicInternalService.getTopicByCode(group.getTopic().getNamespace(), group.getTopic().getCode()) == null) {
            throw new NsrException(String.format("topic: %s does not exist", group.getTopic()));
        }

        PartitionGroup oldPartitionGroup = partitionGroupInternalService.getByTopicAndGroup(group.getTopic(), group.getGroup());
        if (oldPartitionGroup == null) {
            throw new NsrException(String.format("topic: %s, group: %s does not exist", group.getTopic(), group.getGroup()));
        }

        Broker leader = brokerInternalService.getById(group.getLeader());
        if (leader == null) {
            throw new NsrException(String.format("topic: %s, group: %s, broker: {} does not exist", group.getTopic(), group.getGroup(), group.getLeader()));
        }

        logger.info("leaderChange, topic: {}, partitionGroup: {}", group.getTopic(), group);
        Broker oldLeader = brokerInternalService.getById(oldPartitionGroup.getLeader());
        if (oldLeader == null) {
            throw new NsrException(String.format("topic: %s, group: %s, broker: {} does not exist", group.getTopic(), group.getGroup(), group.getLeader()));
        }

        try {
            transactionInternalService.begin();
        } catch (Exception e) {
            logger.error("beginTransaction exception, topic: {}, partitionGroup: {}", group.getTopic(), group, e);
            throw new NsrException(e);
        }

        try {
            topicInternalService.leaderChange(group);
            transactionInternalService.commit();
        } catch (Exception e) {
            logger.error("leaderChange exception, topic: {}, partitionGroup: {}", group.getTopic(), group.getGroup(), e);
            transactionInternalService.rollback();
            throw new NsrException(e);
        }

        if (config.getMessengerPublishLeaderChangeEnable()) {
            messenger.publish(new LeaderChangeEvent(group.getTopic(), oldPartitionGroup, group), oldLeader);
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
            throw new NsrException(String.format("topic: %s does not exist", topic.getName()));
        }

        logger.info("update, topic: {}", topic);

        List<PartitionGroup> partitionGroups = partitionGroupInternalService.getByTopic(topic.getName());
        List<Broker> replicas = getReplicas(partitionGroups);

        try {
            transactionInternalService.begin();
        } catch (Exception e) {
            logger.error("beginTransaction exception, topic: {}", topic, e);
            throw new NsrException(e);
        }

        try {
            topicInternalService.update(topic);
            transactionInternalService.commit();
        } catch (Exception e) {
            logger.error("removeTopic exception, topic: {}, partitionGroups: {}", topic, partitionGroups, e);
            if (config.getMessengerPublishTopicEnable()) {
                messenger.publish(new UpdateTopicEvent(topic, oldTopic), replicas);
            }
            transactionInternalService.rollback();
            throw new NsrException(e);
        }

        if (config.getMessengerPublishTopicEnable()) {
            messenger.publish(new UpdateTopicEvent(oldTopic, topic), replicas);
        }
        return topic;
    }

    protected void fillBroker(PartitionGroup partitionGroup) {
        fillBroker(Lists.newArrayList(partitionGroup));
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
                    if (broker == null) {
                        throw new NsrException(String.format("broker %d not exist", replica));
                    }
                    brokerMap.put(replica, broker);
                }
            }
            if (partitionGroup.getLearners() != null) {
                for (Integer learner : partitionGroup.getLearners()) {
                    Broker broker = brokersMap.get(learner);
                    if (broker == null) {
                        throw new NsrException(String.format("broker %d not exist", learner));
                    }
                    brokerMap.put(learner, broker);
                }
            }
            partitionGroup.setBrokers(brokerMap);
        }
    }

    protected List<Broker> getReplicas(PartitionGroup partitionGroup) {
        if (CollectionUtils.isNotEmpty(partitionGroup.getReplicas())) {
            return Collections.emptyList();
        }
        return brokerInternalService.getByIds(Lists.newArrayList(partitionGroup.getReplicas()));
    }

    protected List<Broker> getReplicas(List<PartitionGroup> partitionGroups) {
        List<Integer> brokerIds = Lists.newArrayList();
        for (PartitionGroup partitionGroup : partitionGroups) {
            if (CollectionUtils.isNotEmpty(partitionGroup.getReplicas())) {
                brokerIds.addAll(partitionGroup.getReplicas());
            }
        }
        if (CollectionUtils.isEmpty(brokerIds)) {
            return Collections.emptyList();
        }
        return brokerInternalService.getByIds(brokerIds);
    }
}
