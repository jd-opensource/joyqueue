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

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.joyqueue.client.internal.cluster.ClusterClientManager;
import org.joyqueue.client.internal.consumer.converter.BrokerAssignmentConverter;
import org.joyqueue.client.internal.consumer.coordinator.domain.BrokerAssignments;
import org.joyqueue.client.internal.consumer.coordinator.domain.BrokerAssignmentsHolder;
import org.joyqueue.client.internal.metadata.domain.TopicMetadata;
import org.joyqueue.network.domain.BrokerNode;
import org.joyqueue.toolkit.service.Service;
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * ConsumerCoordinator
 *
 * author: gaohaoxiang
 * date: 2018/12/6
 */
public class ConsumerCoordinator extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(ConsumerCoordinator.class);

    private ClusterClientManager clusterClientManager;
    private PartitionAssignmentManager partitionAssignmentManager;
    private CoordinatorManager coordinatorManager;

    private Table<String /** app **/, String /** topic **/, BrokerAssignmentsHolder> brokerAssignmentCache = HashBasedTable.create();

    public ConsumerCoordinator(ClusterClientManager clusterClientManager) {
        this.clusterClientManager = clusterClientManager;
    }

    @Override
    protected void validate() throws Exception {
        coordinatorManager = new CoordinatorManager(clusterClientManager);
        partitionAssignmentManager = new PartitionAssignmentManager(clusterClientManager, coordinatorManager);
    }

    @Override
    protected void doStart() throws Exception {
        coordinatorManager.start();
        partitionAssignmentManager.start();
    }

    @Override
    protected void doStop() {
        if (coordinatorManager != null) {
            coordinatorManager.stop();
        }
        if (partitionAssignmentManager != null) {
            partitionAssignmentManager.stop();
        }
    }

    public BrokerAssignments fetchBrokerAssignment(TopicMetadata topicMetadata, String app, long sessionTimeout) {
        BrokerAssignmentsHolder brokerAssignmentsHolder = brokerAssignmentCache.get(app, topicMetadata.getTopic());
        if (brokerAssignmentsHolder != null && !brokerAssignmentsHolder.isExpired(sessionTimeout)) {
            return brokerAssignmentsHolder.getBrokerAssignments();
        }

        synchronized (this) {
            brokerAssignmentsHolder = brokerAssignmentCache.get(app, topicMetadata.getTopic());
            if (brokerAssignmentsHolder != null && !brokerAssignmentsHolder.isExpired(sessionTimeout)) {
                return brokerAssignmentsHolder.getBrokerAssignments();
            }

            BrokerAssignments brokerAssignments = partitionAssignmentManager.fetchBrokerAssignment(topicMetadata, app, sessionTimeout);
            brokerAssignmentsHolder = new BrokerAssignmentsHolder(brokerAssignments, SystemClock.now());
            brokerAssignmentCache.put(app, topicMetadata.getTopic(), brokerAssignmentsHolder);

            if (logger.isDebugEnabled()) {
                logger.debug("update consumer assignments, app: {}, topic: {}, assignments: {}", app, topicMetadata.getTopic(), brokerAssignments);
            }

            return brokerAssignments;
        }
    }

    public BrokerAssignments fetchAllBrokerAssignments(TopicMetadata topicMetadata, String app) {
        return buildAllBrokerAssignments(topicMetadata);
    }

    public Map<String, BrokerNode> findCoordinators(List<String> topics, String app) {
        return coordinatorManager.findCoordinators(topics, app);
    }

    protected BrokerAssignments buildAllBrokerAssignments(TopicMetadata topicMetadata) {
        return BrokerAssignmentConverter.convertTopicAssignments(topicMetadata);
    }
}