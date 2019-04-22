package com.jd.journalq.broker.coordinator.support;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.broker.coordinator.config.CoordinatorConfig;
import com.jd.journalq.domain.Broker;
import com.jd.journalq.domain.ClientType;
import com.jd.journalq.domain.PartitionGroup;
import com.jd.journalq.domain.Subscription;
import com.jd.journalq.domain.Topic;
import com.jd.journalq.domain.TopicConfig;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.nsr.NameService;
import com.jd.journalq.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * CoordinatorInitializer
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/4
 */
public class CoordinatorInitializer extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(CoordinatorInitializer.class);

    private CoordinatorConfig config;
    private ClusterManager clusterManager;
    private NameService nameService;

    public CoordinatorInitializer(CoordinatorConfig config, ClusterManager clusterManager, NameService nameService) {
        this.config = config;
        this.clusterManager = clusterManager;
        this.nameService = nameService;
    }

    public boolean init() {
        try {
            initGroupTopic();
            initTransactionTopic();
            return true;
        } catch (Exception e) {
            logger.error("init coordinator exception", e);
            return false;
        }
    }

    protected boolean initGroupTopic() {
        return initCoordinatorTopic(config.getGroupTopic(), config.getGroupTopicPartitions());
    }

    protected boolean initTransactionTopic() {
        return initCoordinatorTopic(config.getTransactionTopic(), config.getTransactionTopicPartitions())
                && initCoordinatorSubscribe(config.getTransactionTopic(), config.getTransactionLogApp());
    }

    protected boolean initCoordinatorSubscribe(TopicName topic, String app) {
        if (nameService.getConsumerByTopicAndApp(topic, app) == null) {
            nameService.subscribe(new Subscription(topic, app, Subscription.Type.CONSUMPTION), ClientType.JMQ);
        }
        if (nameService.getProducerByTopicAndApp(topic, app) == null) {
            nameService.subscribe(new Subscription(topic, app, Subscription.Type.PRODUCTION), ClientType.JMQ);
        }
        return true;
    }

    protected boolean initCoordinatorTopic(TopicName topic, int partitions) {
        TopicConfig coordinatorTopic = nameService.getTopicConfig(topic);
        if (coordinatorTopic != null) {
            return true;
        }

        Broker currentBroker = clusterManager.getBroker();
        List<PartitionGroup> partitionGroupList = Lists.newLinkedList();
        for (int i = 0; i < partitions; i++) {
            PartitionGroup partitionGroup = new PartitionGroup();
            partitionGroup.setTopic(topic);
            partitionGroup.setGroup(i);
            partitionGroup.setLeader(currentBroker.getId());
            partitionGroup.setPartitions(Sets.newHashSet((short) i));
            partitionGroup.setReplicas(Sets.newHashSet(currentBroker.getId()));
            partitionGroupList.add(partitionGroup);
        }

        Topic newTopic = new Topic();
        newTopic.setName(topic);
        newTopic.setPartitions((short) partitionGroupList.size());
        newTopic.setType(TopicConfig.Type.TOPIC);

        logger.info("create coordinator topic, topic: {}, partitions: {}", newTopic.getName().getFullName(), newTopic.getPartitions());

        nameService.addTopic(newTopic, partitionGroupList);
        return true;
    }
}