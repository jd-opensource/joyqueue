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
package org.joyqueue.client.internal.consumer.coordinator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joyqueue.client.internal.cluster.ClusterClientManager;
import org.joyqueue.client.internal.consumer.coordinator.domain.BrokerAssignment;
import org.joyqueue.client.internal.consumer.coordinator.domain.BrokerAssignments;
import org.joyqueue.client.internal.consumer.coordinator.domain.PartitionAssignment;
import org.joyqueue.client.internal.metadata.domain.PartitionMetadata;
import org.joyqueue.client.internal.metadata.domain.TopicMetadata;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.network.command.FetchAssignedPartitionAckData;
import org.joyqueue.network.command.FetchAssignedPartitionResponse;
import org.joyqueue.network.domain.BrokerNode;
import org.joyqueue.toolkit.service.Service;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * PartitionAssignmentManager
 *
 * author: gaohaoxiang
 * date: 2018/12/11
 */
public class PartitionAssignmentManager extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(PartitionAssignmentManager.class);

    private ClusterClientManager clusterClientManager;
    private CoordinatorManager coordinatorManager;

    public PartitionAssignmentManager(ClusterClientManager clusterClientManager, CoordinatorManager coordinatorManager) {
        this.clusterClientManager = clusterClientManager;
        this.coordinatorManager = coordinatorManager;
    }

    public BrokerAssignments fetchBrokerAssignment(TopicMetadata topicMetadata, String app, long sessionTimeout) {
        PartitionAssignment partitionAssignment = fetchPartitionAssignment(topicMetadata, app, sessionTimeout);

        if (partitionAssignment == null || CollectionUtils.isEmpty(partitionAssignment.getPartitions())) {
            return new BrokerAssignments();
        }

        BrokerAssignments brokerAssignments = new BrokerAssignments();
        Map<BrokerNode, List<Short>> brokerPartitions = Maps.newHashMap();

        for (Short partition : partitionAssignment.getPartitions()) {
            PartitionMetadata partitionMetadata = topicMetadata.getPartition(partition);
            if (partitionMetadata.getLeader() == null || !partitionMetadata.getLeader().isReadable()) {
                continue;
            }
            List<Short> brokerPartitionList = brokerPartitions.get(partitionMetadata.getLeader());
            if (brokerPartitionList == null) {
                brokerPartitionList = Lists.newLinkedList();
                brokerPartitions.put(partitionMetadata.getLeader(), brokerPartitionList);
            }
            brokerPartitionList.add(partition);
        }

        List<BrokerAssignment> assignments = Lists.newArrayListWithCapacity(brokerPartitions.size());
        for (Map.Entry<BrokerNode, List<Short>> brokerEntry : brokerPartitions.entrySet()) {
            assignments.add(new BrokerAssignment(brokerEntry.getKey(), new PartitionAssignment(brokerEntry.getValue())));
        }

        brokerAssignments.setAssignments(assignments);
        return brokerAssignments;
    }

    public PartitionAssignment fetchPartitionAssignment(TopicMetadata topicMetadata, String app, long sessionTimeout) {
        try {
            BrokerNode coordinator = coordinatorManager.findCoordinator(topicMetadata.getTopic(), app);
            if (coordinator == null) {
                logger.warn("fetch partition assignment error, coordinator not exist, topic: {}, app: {}", topicMetadata.getTopic(), app);
                return null;
            }

            FetchAssignedPartitionResponse fetchAssignedPartitionResponse = coordinatorManager.fetchAssignedPartition(coordinator,
                    topicMetadata.getTopic(), app, topicMetadata.getConsumerPolicy().getNearby(), sessionTimeout);
            FetchAssignedPartitionAckData fetchAssignedPartitionAckData = fetchAssignedPartitionResponse.getTopicPartitions().get(topicMetadata.getTopic());

            if (fetchAssignedPartitionAckData == null) {
                logger.warn("fetch partition assignment error, no partitions, topic: {}, app: {}", topicMetadata.getTopic(), app);
                return null;
            } else if (!fetchAssignedPartitionAckData.getCode().equals(JoyQueueCode.SUCCESS)) {
                logger.warn("fetch partition assignment error, topic: {}, app: {}, error: {}", topicMetadata.getTopic(), app, fetchAssignedPartitionAckData.getCode().getMessage());
                return null;
            }

            PartitionAssignment partitionAssignment = new PartitionAssignment();
            partitionAssignment.setPartitions(fetchAssignedPartitionAckData.getPartitions());
            return partitionAssignment;
        } catch (Exception e) {
            logger.error("fetch partition assignment exception, topic: {}, app: {}, error: {}", topicMetadata.getTopic(), app, e.getMessage());
            logger.debug("fetch partition assignment exception, topic: {}, app: {}", topicMetadata.getTopic(), app, e);
            return null;
        }
    }
}