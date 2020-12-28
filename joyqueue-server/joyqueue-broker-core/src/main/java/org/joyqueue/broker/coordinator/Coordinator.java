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
package org.joyqueue.broker.coordinator;

import org.apache.commons.collections.CollectionUtils;
import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.coordinator.config.CoordinatorConfig;
import org.joyqueue.broker.coordinator.domain.CoordinatorDetail;
import org.joyqueue.broker.coordinator.support.CoordinatorInitializer;
import org.joyqueue.broker.coordinator.support.CoordinatorResolver;
import org.joyqueue.domain.Broker;
import org.joyqueue.domain.Consumer;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.TopicConfig;
import org.joyqueue.domain.TopicName;
import org.joyqueue.network.transport.session.session.TransportSessionManager;
import org.joyqueue.nsr.NameService;

import java.util.List;

/**
 * Coordinator
 *
 * author: gaohaoxiang
 * date: 2018/12/4
 */
public class Coordinator {

    private CoordinatorConfig config;
    private ClusterManager clusterManager;
    private NameService nameService;
    private CoordinatorResolver coordinatorResolver;
    private CoordinatorInitializer coordinatorInitializer;
    private TransportSessionManager coordinatorSessionManager;

    public Coordinator(CoordinatorConfig config, ClusterManager clusterManager, CoordinatorResolver coordinatorResolver,
                       CoordinatorInitializer coordinatorInitializer, TransportSessionManager coordinatorSessionManager) {
        this.config = config;
        this.clusterManager = clusterManager;
        this.nameService = clusterManager.getNameService();
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
        String namespace = resolveGroupNamespace(group);
        return coordinatorResolver.findCoordinator(group, config.getGroupTopic(namespace));
    }

    public CoordinatorDetail getGroupDetail(String group) {
        String namespace = resolveGroupNamespace(group);
        return coordinatorResolver.getCoordinatorDetail(group, config.getGroupTopic(namespace));
    }

    protected String resolveGroupNamespace(String group) {
        List<Consumer> consumers = nameService.getConsumersByApp(group);
        if (CollectionUtils.isEmpty(consumers)) {
            return null;
        }
        Consumer consumer = consumers.get(0);
        if (consumer.getConsumerPolicy() == null) {
            return null;
        }
        return consumer.getConsumerPolicy().getRegion();
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

    public TopicName getTransactionTopic() {
        return config.getTransactionTopic();
    }

    public TopicConfig getTransactionTopicConfig() {
        return nameService.getTopicConfig(config.getTransactionTopic());
    }

    public PartitionGroup getTransactionPartitionGroup(String key) {
        return coordinatorResolver.resolveCoordinatorPartitionGroup(key, config.getTransactionTopic());
    }

    public boolean initCoordinator() {
        return coordinatorInitializer.init();
    }

    public TransportSessionManager getSessionManager() {
        return coordinatorSessionManager;
    }
}