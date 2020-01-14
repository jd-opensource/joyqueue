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
package org.joyqueue.client.internal.transport;

import com.google.common.collect.Sets;
import org.joyqueue.network.domain.BrokerNode;

import java.util.Collection;
import java.util.Set;

/**
 * ConnectionState
 *
 * author: gaohaoxiang
 * date: 2019/2/12
 */
public class ConnectionState {

    private Set<BrokerNode> brokerNodes = Sets.newConcurrentHashSet();
    private Set<String> topics = Sets.newConcurrentHashSet();
    private Set<String> apps = Sets.newConcurrentHashSet();

    public boolean addBrokerNode(BrokerNode brokerNode) {
        return brokerNodes.add(brokerNode);
    }

    public boolean addTopics(Collection<String> topics) {
        return this.topics.addAll(topics);
    }

    public boolean addApp(String app) {
        return apps.add(app);
    }

    public Set<BrokerNode> getBrokerNodes() {
        return brokerNodes;
    }

    public Set<String> getTopics() {
        return topics;
    }

    public Set<String> getApps() {
        return apps;
    }
}