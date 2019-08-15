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
package io.chubao.joyqueue.broker.kafka.coordinator;

import io.chubao.joyqueue.broker.coordinator.session.CoordinatorSessionManager;
import io.chubao.joyqueue.domain.Broker;
import io.chubao.joyqueue.domain.PartitionGroup;
import io.chubao.joyqueue.domain.TopicConfig;
import io.chubao.joyqueue.domain.TopicName;

/**
 * Coordinator
 *
 * author: gaohaoxiang
 * date: 2018/12/4
 */
public class Coordinator {

    private io.chubao.joyqueue.broker.coordinator.Coordinator coordinator;

    public Coordinator(io.chubao.joyqueue.broker.coordinator.Coordinator coordinator) {
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

    public CoordinatorSessionManager getSessionManager() {
        return coordinator.getSessionManager();
    }
}