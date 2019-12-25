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
package org.joyqueue.broker.protocol;

import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.polling.LongPollingManager;
import org.joyqueue.broker.protocol.config.JoyQueueConfig;
import org.joyqueue.broker.protocol.coordinator.Coordinator;
import org.joyqueue.broker.protocol.coordinator.GroupMetadataManager;
import org.joyqueue.broker.protocol.coordinator.assignment.PartitionAssignmentHandler;

/**
 * JoyQueueContext
 *
 * author: gaohaoxiang
 * date: 2018/11/28
 */
public class JoyQueueContext {

    private static JoyQueueConfig config;
    private Coordinator coordinator;
    private GroupMetadataManager groupMetadataManager;
    private PartitionAssignmentHandler partitionAssignmentHandler;
    private LongPollingManager longPollingManager;
    private BrokerContext brokerContext;

    public JoyQueueContext(JoyQueueConfig config, Coordinator coordinator, GroupMetadataManager groupMetadataManager, PartitionAssignmentHandler partitionAssignmentHandler,
                           LongPollingManager longPollingManager, BrokerContext brokerContext) {
        this.config = config;
        this.coordinator = coordinator;
        this.groupMetadataManager = groupMetadataManager;
        this.partitionAssignmentHandler = partitionAssignmentHandler;
        this.longPollingManager = longPollingManager;
        this.brokerContext = brokerContext;
    }

    public static JoyQueueConfig getConfig() {
        return config;
    }

    public Coordinator getCoordinator() {
        return coordinator;
    }

    public GroupMetadataManager getGroupMetadataManager() {
        return groupMetadataManager;
    }

    public PartitionAssignmentHandler getPartitionAssignmentHandler() {
        return partitionAssignmentHandler;
    }

    public LongPollingManager getLongPollingManager() {
        return longPollingManager;
    }

    public BrokerContext getBrokerContext() {
        return brokerContext;
    }
}