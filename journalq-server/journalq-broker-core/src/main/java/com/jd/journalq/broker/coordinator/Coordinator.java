/**
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
package com.jd.journalq.broker.coordinator;

import com.jd.journalq.broker.coordinator.config.CoordinatorConfig;
import com.jd.journalq.broker.coordinator.domain.CoordinatorDetail;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.domain.Broker;
import com.jd.journalq.domain.TopicName;

/**
 * Coordinator
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/4
 */
public class Coordinator {

    private CoordinatorConfig config;
    private ClusterManager clusterManager;
    private CoordinatorResolver coordinatorResolver;
    private CoordinatorInitializer coordinatorInitializer;

    public Coordinator(CoordinatorConfig config, ClusterManager clusterManager, CoordinatorResolver coordinatorResolver, CoordinatorInitializer coordinatorInitializer) {
        this.config = config;
        this.clusterManager = clusterManager;
        this.coordinatorResolver = coordinatorResolver;
        this.coordinatorInitializer = coordinatorInitializer;
    }

    public boolean isCurrentCoordinator(String key) {
        Broker coordinatorBroker = findCoordinator(key);
        return clusterManager.getBroker().equals(coordinatorBroker);
    }

    public Broker findCoordinator(String key) {
        return coordinatorResolver.resolve(key);
    }

    public CoordinatorDetail getCoordinatorDetail(String key) {
        return coordinatorResolver.resolveDetail(key);
    }

    public boolean isCoordinatorTopic(TopicName topic) {
        return config.getTopic().getFullName().equals(topic);
    }

    public boolean initCoordinator() {
        return coordinatorInitializer.init();
    }
}