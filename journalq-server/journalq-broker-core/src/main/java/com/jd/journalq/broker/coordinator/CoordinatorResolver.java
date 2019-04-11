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
import com.google.common.collect.Maps;
import com.jd.journalq.broker.coordinator.config.CoordinatorConfig;
import com.jd.journalq.broker.coordinator.domain.CoordinatorDetail;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.domain.Broker;
import com.jd.journalq.domain.PartitionGroup;
import com.jd.journalq.domain.TopicConfig;
import com.jd.journalq.toolkit.service.Service;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * CoordinatorResolver
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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

    public Broker resolve(String group) {
        PartitionGroup coordinatorPartitionGroup = resolvePartitionGroup(group);
        if (coordinatorPartitionGroup == null) {
            return null;
        }
        return coordinatorPartitionGroup.getLeaderBroker();
    }

    public CoordinatorDetail resolveDetail(String group) {
        PartitionGroup coordinatorPartitionGroup = resolvePartitionGroup(group);
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

    protected PartitionGroup resolvePartitionGroup(String group) {
        TopicConfig coordinatorTopic = clusterManager.getNameService().getTopicConfig(config.getTopic());
        if (coordinatorTopic == null) {
            return null;
        }
        return resolvePartitionGroup(group, coordinatorTopic);
    }

    protected PartitionGroup resolvePartitionGroup(String group, TopicConfig topicConfig) {
        List<PartitionGroup> partitionGroups = null;
        if (topicConfig.getPartitionGroups() instanceof List) {
            partitionGroups = (List) topicConfig.getPartitionGroups();
        } else {
            partitionGroups = Lists.newArrayList(topicConfig.getPartitionGroups().values());
        }
        short index = (short) Math.abs(group.hashCode() % partitionGroups.size());
        return partitionGroups.get(index);
    }
}