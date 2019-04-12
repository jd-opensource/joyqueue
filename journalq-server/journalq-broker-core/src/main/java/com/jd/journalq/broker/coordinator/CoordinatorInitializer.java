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
package com.jd.journalq.broker.coordinator;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.broker.coordinator.config.CoordinatorConfig;
import com.jd.journalq.domain.Broker;
import com.jd.journalq.domain.PartitionGroup;
import com.jd.journalq.domain.Topic;
import com.jd.journalq.domain.TopicConfig;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.nsr.NameService;
import com.jd.journalq.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

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
            return initTopic();
        } catch (Exception e) {
            logger.error("init coordinator exception", e);
            return false;
        }
    }

    public boolean initTopic() {
        return tempInitTopic();
    }

    protected boolean initTopicInternal() {
        TopicName coordinatorTopicName = config.getTopic();
        TopicConfig coordinatorTopic = clusterManager.getNameService().getTopicConfig(coordinatorTopicName);
        if (coordinatorTopic != null && coordinatorTopic.getPartitions() >= config.getTopicPartitions()) {
            return false;
        }

        List<PartitionGroup> partitionGroupList = Lists.newLinkedList();
        List<Broker> allBrokers = nameService.getAllBrokers();
        for (int i = 0, len = (Math.min(allBrokers.size(), config.getTopicPartitions())); i < len; i++) {
            Broker broker = allBrokers.get(i);
            PartitionGroup partitionGroup = new PartitionGroup();
            partitionGroup.setTopic(coordinatorTopicName);
            partitionGroup.setGroup(i);
            partitionGroup.setLeader(broker.getId());
            partitionGroup.setPartitions(Sets.newHashSet((short) i));
            partitionGroup.setReplicas(Sets.newHashSet(allBrokers.stream().map(Broker::getId).collect(Collectors.toList())));
            partitionGroupList.add(partitionGroup);
        }

        Topic topic = new Topic();
        topic.setName(coordinatorTopicName);
        topic.setPartitions((short) partitionGroupList.size());
        topic.setType(TopicConfig.Type.TOPIC);

        logger.info("create coordinator topic, topic: {}, partitions: {}", topic.getName().getFullName(), topic.getPartitions());

        nameService.addTopic(topic, partitionGroupList);

        logger.info("create coordinator topic complete");
        return true;
    }

    protected boolean tempInitTopic() {
        TopicName coordinatorTopicName = config.getTopic();
        TopicConfig coordinatorTopic = clusterManager.getNameService().getTopicConfig(coordinatorTopicName);
        if (coordinatorTopic != null) {
            return false;
        }

        List<PartitionGroup> partitionGroupList = Lists.newLinkedList();
        for (int i = 0, len = config.getTopicPartitions(); i < len; i++) {
            Broker broker = clusterManager.getBroker();
            PartitionGroup partitionGroup = new PartitionGroup();
            partitionGroup.setTopic(coordinatorTopicName);
            partitionGroup.setGroup(i);
            partitionGroup.setLeader(broker.getId());
            partitionGroup.setPartitions(Sets.newHashSet((short) i));
            partitionGroup.setReplicas(Sets.newHashSet(broker.getId()));
            partitionGroupList.add(partitionGroup);
        }

        Topic topic = new Topic();
        topic.setName(coordinatorTopicName);
        topic.setPartitions((short) partitionGroupList.size());
        topic.setType(TopicConfig.Type.TOPIC);

        logger.info("create coordinator topic, topic: {}, partitions: {}", topic.getName().getFullName(), topic.getPartitions());

        nameService.addTopic(topic, partitionGroupList);

        logger.info("create coordinator topic complete");
        return true;
    }
}