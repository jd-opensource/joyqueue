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
package org.joyqueue.client.internal.metadata.domain;

import org.joyqueue.network.domain.BrokerNode;

import java.util.Map;

/**
 * ClusterMetadata
 *
 * author: gaohaoxiang
 * date: 2018/11/30
 */
public class ClusterMetadata {

    private Map<String, TopicMetadata> topics;
    private Map<Integer, BrokerNode> brokers;

    public ClusterMetadata(Map<String, TopicMetadata> topics, Map<Integer, BrokerNode> brokers) {
        this.topics = topics;
        this.brokers = brokers;
    }

    public TopicMetadata getTopic(String code) {
        return topics.get(code);
    }

    public BrokerNode getBrokerNode(int broker) {
        return brokers.get(broker);
    }

    public Map<String, TopicMetadata> getTopics() {
        return topics;
    }

    public Map<Integer, BrokerNode> getBrokers() {
        return brokers;
    }
}