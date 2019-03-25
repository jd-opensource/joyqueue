package com.jd.journalq.client.internal.consumer.coordinator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jd.journalq.client.internal.cluster.ClusterClientManager;
import com.jd.journalq.client.internal.consumer.coordinator.domain.BrokerAssignment;
import com.jd.journalq.client.internal.consumer.coordinator.domain.BrokerAssignments;
import com.jd.journalq.client.internal.consumer.coordinator.domain.PartitionAssignment;
import com.jd.journalq.client.internal.metadata.domain.PartitionMetadata;
import com.jd.journalq.client.internal.metadata.domain.TopicMetadata;
import com.jd.journalq.common.exception.JMQCode;
import com.jd.journalq.common.network.command.FetchAssignedPartitionAck;
import com.jd.journalq.common.network.command.FetchAssignedPartitionAckData;
import com.jd.journalq.common.network.domain.BrokerNode;
import com.jd.journalq.toolkit.service.Service;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * PartitionAssignmentManager
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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
            if (partitionMetadata.getLeader() == null) {
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

            FetchAssignedPartitionAck fetchAssignedPartitionAck = coordinatorManager.fetchAssignedPartition(coordinator, topicMetadata.getTopic(), app, topicMetadata.getConsumerPolicy().getNearby(), sessionTimeout);
            FetchAssignedPartitionAckData fetchAssignedPartitionAckData = fetchAssignedPartitionAck.getTopicPartitions().get(topicMetadata.getTopic());

            if (fetchAssignedPartitionAckData == null) {
                logger.warn("fetch partition assignment error, no partitions, topic: {}, app: {}", topicMetadata.getTopic(), app);
                return null;
            } else if (!fetchAssignedPartitionAckData.getCode().equals(JMQCode.SUCCESS)) {
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