package com.jd.journalq.broker.coordinator.support;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.broker.coordinator.config.CoordinatorConfig;
import com.jd.journalq.broker.coordinator.domain.CoordinatorDetail;
import com.jd.journalq.domain.Broker;
import com.jd.journalq.domain.PartitionGroup;
import com.jd.journalq.domain.TopicConfig;
import com.jd.journalq.domain.TopicName;
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