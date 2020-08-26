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
package org.joyqueue.broker.kafka.coordinator;

import org.joyqueue.domain.Broker;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.TopicConfig;
import org.joyqueue.domain.TopicName;
import org.joyqueue.network.transport.session.session.TransportSessionManager;

/**
 * Coordinator
 *
 * author: gaohaoxiang
 * date: 2018/12/4
 */
public class Coordinator {

    private org.joyqueue.broker.coordinator.Coordinator coordinator;

    public Coordinator(org.joyqueue.broker.coordinator.Coordinator coordinator) {
        this.coordinator = coordinator;
    }

    public Broker findGroup(String groupId) {
        return coordinator.findGroup(groupId);
    }

    public boolean isCurrentGroup(String groupId) {
        return coordinator.isCurrentGroup(groupId);
    }

    public Broker findTransaction(String transactionId) {
        return coordinator.findTransaction(transactionId);
    }

    public boolean isCurrentTransaction(String transactionId) {
        return coordinator.isCurrentTransaction(transactionId);
    }

    public TopicConfig getTransactionTopicConfig() {
        return coordinator.getTransactionTopicConfig();
    }

    public PartitionGroup getTransactionPartitionGroup(String transactionId) {
        return coordinator.getTransactionPartitionGroup(transactionId);
    }

    public TopicName getTransactionTopic() {
        return coordinator.getTransactionTopic();
    }

    public TransportSessionManager getSessionManager() {
        return coordinator.getSessionManager();
    }
}