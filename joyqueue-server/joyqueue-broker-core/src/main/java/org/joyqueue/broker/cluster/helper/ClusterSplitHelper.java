package org.joyqueue.broker.cluster.helper;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.joyqueue.broker.cluster.ClusterNodeManager;
import org.joyqueue.broker.cluster.entry.ClusterNode;
import org.joyqueue.broker.cluster.entry.ClusterPartitionGroup;
import org.joyqueue.broker.cluster.entry.SplittedCluster;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.TopicConfig;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * ClusterSplitHelper
 * author: gaohaoxiang
 * date: 2020/3/27
 */
public class ClusterSplitHelper {

    public static Map<Integer /** brokerId **/, List<Integer /** group **/>> splitByReWrite(TopicConfig topicConfig) {
        Map<Integer /** brokerId **/, List<Integer /** group **/>> result = Maps.newHashMap();
        for (Map.Entry<Integer, PartitionGroup> entry : topicConfig.getPartitionGroups().entrySet()) {
            ClusterPartitionGroup clusterPartitionGroup = (ClusterPartitionGroup) entry.getValue();
            if (clusterPartitionGroup.isRewrite()) {
                continue;
            }
            for (Integer replica : clusterPartitionGroup.getReplicas()) {
                List<Integer> groups = result.get(replica);
                if (groups == null) {
                    groups = Lists.newLinkedList();
                    result.put(replica, groups);
                }
                groups.add(clusterPartitionGroup.getGroup());
            }
        }
        return result;
    }

    public static TopicConfig cloneTopicConfig(TopicConfig topicConfig) {
        TopicConfig result = TopicConfig.toTopicConfig(topicConfig);
        Map<Integer, PartitionGroup> partitionGroups = Maps.newHashMap();
        for (Map.Entry<Integer, PartitionGroup> entry : topicConfig.getPartitionGroups().entrySet()) {
            partitionGroups.put(entry.getKey(), new ClusterPartitionGroup(entry.getValue().clone()));
        }
        result.setPartitionGroups(partitionGroups);
        return result;
    }

    public static SplittedCluster split(TopicConfig topicConfig, ClusterNodeManager clusterNodeManager) {
        boolean isLocal = true;
        Map<Integer /** brokerId **/, List<Integer /** group **/>> splittedByGroup = null;
        Map<Integer /** leaderId **/, List<Integer /** group **/>> splittedByLeader = null;

        for (Map.Entry<Integer, PartitionGroup> entry : topicConfig.getPartitionGroups().entrySet()) {
            PartitionGroup partitionGroup = entry.getValue();
            ClusterNode groupNode = clusterNodeManager.getTopicGroupNode(topicConfig.getName().getFullName(), entry.getKey());
            if (groupNode != null) {
                partitionGroup.setLeader(groupNode.getLeader());
            } else {
                if (CollectionUtils.isEmpty(partitionGroup.getReplicas())) {
                    partitionGroup.setLeader(-1);
                } else if (partitionGroup.getReplicas().size() == 1) {
                    partitionGroup.setLeader(partitionGroup.getReplicas().iterator().next());
                } else {
                    isLocal = false;
                    if (splittedByGroup == null) {
                        splittedByGroup = Maps.newHashMap();
                    }
                    for (Integer replica : partitionGroup.getReplicas()) {
                        List<Integer> partitionGroups = splittedByGroup.get(replica);
                        if (partitionGroups == null) {
                            partitionGroups = Lists.newLinkedList();
                            splittedByGroup.put(replica, partitionGroups);
                        }
                        partitionGroups.add(partitionGroup.getGroup());
                    }
                }
            }
        }

        if (splittedByGroup != null) {
            splittedByLeader = Maps.newHashMap();
            for (Map.Entry<Integer, PartitionGroup> entry : topicConfig.getPartitionGroups().entrySet()) {
                Integer leader = entry.getValue().getLeader();
                if (leader != null && !leader.equals(-1)) {
                    List<Integer> groups = splittedByGroup.get(leader);
                    if (groups != null) {
                        splittedByLeader.put(leader, groups);
                    }
                }
            }
        }

        return new SplittedCluster(isLocal, ObjectUtils.defaultIfNull(splittedByGroup, Collections.emptyMap()),
                ObjectUtils.defaultIfNull(splittedByLeader, Collections.emptyMap()));
    }
}