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
package org.joyqueue.broker.coordinator.support;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.coordinator.config.CoordinatorConfig;
import org.joyqueue.domain.Broker;
import org.joyqueue.domain.ClientType;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.Subscription;
import org.joyqueue.domain.Topic;
import org.joyqueue.domain.TopicConfig;
import org.joyqueue.domain.TopicName;
import org.joyqueue.nsr.NameService;
import org.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * CoordinatorInitializer
 *
 * author: gaohaoxiang
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
            nameService.subscribe(new Subscription(topic, app, Subscription.Type.CONSUMPTION), ClientType.JOYQUEUE);
        }
        if (nameService.getProducerByTopicAndApp(topic, app) == null) {
            nameService.subscribe(new Subscription(topic, app, Subscription.Type.PRODUCTION), ClientType.JOYQUEUE);
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
            partitionGroup.setElectType(PartitionGroup.ElectType.raft);
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