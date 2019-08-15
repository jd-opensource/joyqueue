/**
 * Copyright 2018 The JoyQueue Authors.
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
package io.chubao.joyqueue.broker.coordinator;

import io.chubao.joyqueue.broker.cluster.ClusterManager;
import io.chubao.joyqueue.broker.coordinator.config.CoordinatorConfig;
import io.chubao.joyqueue.broker.coordinator.domain.CoordinatorDetail;
import io.chubao.joyqueue.broker.coordinator.session.CoordinatorSessionManager;
import io.chubao.joyqueue.broker.coordinator.support.CoordinatorInitializer;
import io.chubao.joyqueue.broker.coordinator.support.CoordinatorResolver;
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

    private CoordinatorConfig config;
    private ClusterManager clusterManager;
    private CoordinatorResolver coordinatorResolver;
    private CoordinatorInitializer coordinatorInitializer;
    private CoordinatorSessionManager coordinatorSessionManager;

    public Coordinator(CoordinatorConfig config, ClusterManager clusterManager, CoordinatorResolver coordinatorResolver,
                       CoordinatorInitializer coordinatorInitializer, CoordinatorSessionManager coordinatorSessionManager) {
        this.config = config;
        this.clusterManager = clusterManager;
        this.coordinatorResolver = coordinatorResolver;
        this.coordinatorInitializer = coordinatorInitializer;
        this.coordinatorSessionManager = coordinatorSessionManager;
    }

    // group

    public boolean isCurrentGroup(String group) {
        Broker coordinatorBroker = findGroup(group);
        return clusterManager.getBroker().equals(coordinatorBroker);
    }

    public Broker findGroup(String group) {
        return coordinatorResolver.findCoordinator(group, config.getGroupTopic());
    }

    public CoordinatorDetail getGroupDetail(String group) {
        return coordinatorResolver.getCoordinatorDetail(group, config.getGroupTopic());
    }

    public boolean isGroupTopic(TopicName topic) {
        return config.getGroupTopic().getFullName().equals(topic.getFullName());
    }

    // transaction

    public boolean isCurrentTransaction(String key) {
        Broker coordinatorBroker = findTransaction(key);
        return clusterManager.getBroker().equals(coordinatorBroker);
    }

    public Broker findTransaction(String key) {
        return coordinatorResolver.findCoordinator(key, config.getTransactionTopic());
    }

    public CoordinatorDetail getTransactionDetail(String key) {
        return coordinatorResolver.getCoordinatorDetail(key, config.getTransactionTopic());
    }

    public boolean isTransactionTopic(TopicName topic) {
        return config.getTransactionTopic().getFullName().equals(topic.getFullName());
    }

    public TopicName getTransactionTopic() {
        return config.getTransactionTopic();
    }

    public TopicConfig getTransactionTopicConfig() {
        return clusterManager.getNameService().getTopicConfig(config.getTransactionTopic());
    }

    public PartitionGroup getTransactionPartitionGroup(String key) {
        return coordinatorResolver.resolveCoordinatorPartitionGroup(key, config.getTransactionTopic());
    }

    public boolean initCoordinator() {
        return coordinatorInitializer.init();
    }

    public CoordinatorSessionManager getSessionManager() {
        return coordinatorSessionManager;
    }
}