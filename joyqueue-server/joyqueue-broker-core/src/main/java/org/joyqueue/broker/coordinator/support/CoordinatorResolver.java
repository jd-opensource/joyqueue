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
import com.google.common.collect.Maps;
import org.apache.commons.lang3.ObjectUtils;
import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.coordinator.config.CoordinatorConfig;
import org.joyqueue.broker.coordinator.domain.CoordinatorDetail;
import org.joyqueue.domain.Broker;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.TopicConfig;
import org.joyqueue.domain.TopicName;
import org.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * CoordinatorResolver
 *
 * author: gaohaoxiang
 * date: 2018/11/6
 */
public class CoordinatorResolver extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(CoordinatorResolver.class);

    private CoordinatorConfig config;
    private ClusterManager clusterManager;

    public CoordinatorResolver(CoordinatorConfig config, ClusterManager clusterManager) {
        this.config = config;
        this.clusterManager = clusterManager;
    }

    public Broker findCoordinator(String key, TopicName topic) {
        PartitionGroup coordinatorPartitionGroup = resolveCoordinatorPartitionGroup(key, topic);
        if (coordinatorPartitionGroup == null) {
            return null;
        }
        return coordinatorPartitionGroup.getLeaderBroker();
    }

    public CoordinatorDetail getCoordinatorDetail(String key, TopicName topic) {
        PartitionGroup coordinatorPartitionGroup = resolveCoordinatorPartitionGroup(key, topic);
        if (coordinatorPartitionGroup == null) {
            return null;
        }

        Map<Integer, Broker> brokers = Maps.newHashMap(ObjectUtils.defaultIfNull(coordinatorPartitionGroup.getBrokers(), Collections.emptyMap()));
        Broker leader = coordinatorPartitionGroup.getLeaderBroker();
        if (leader != null) {
            brokers.remove(leader.getId());
        }
        return new CoordinatorDetail(coordinatorPartitionGroup.getTopic(), coordinatorPartitionGroup.getGroup(), leader, Lists.newArrayList(brokers.values()));
    }

    public PartitionGroup resolveCoordinatorPartitionGroup(String key, TopicName topic) {
        TopicConfig coordinatorTopic = clusterManager.getNameService().getTopicConfig(topic);
        if (coordinatorTopic == null) {
            return null;
        }
        return resolveCoordinatorPartitionGroup(key, coordinatorTopic);
    }

    public PartitionGroup resolveCoordinatorPartitionGroup(String key, TopicConfig topicConfig) {
        List<PartitionGroup> partitionGroups = null;
        if (topicConfig.getPartitionGroups() instanceof List) {
            partitionGroups = (List) topicConfig.getPartitionGroups();
        } else {
            partitionGroups = Lists.newArrayList(topicConfig.getPartitionGroups().values());
        }
        short index = (short) Math.abs(key.hashCode() % partitionGroups.size());
        return partitionGroups.get(index);
    }
}