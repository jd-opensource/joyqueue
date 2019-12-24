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
package org.joyqueue.network.command;

import org.joyqueue.network.domain.BrokerNode;
import org.joyqueue.network.transport.command.JoyQueuePayload;

import java.util.Map;

/**
 * GetClusterAck
 *
 * author: gaohaoxiang
 * date: 2018/11/30
 */
public class FetchClusterResponse extends JoyQueuePayload {

    private Map<String, Topic> topics;
    private Map<Integer, BrokerNode> brokers;

    @Override
    public int type() {
        return JoyQueueCommandType.FETCH_CLUSTER_RESPONSE.getCode();
    }

    public Map<String, Topic> getTopics() {
        return topics;
    }

    public void setTopics(Map<String, Topic> topics) {
        this.topics = topics;
    }

    public Map<Integer, BrokerNode> getBrokers() {
        return brokers;
    }

    public void setBrokers(Map<Integer, BrokerNode> brokers) {
        this.brokers = brokers;
    }

    @Override
    public String toString() {
        return "FetchClusterResponse{" +
                "topics=" + topics +
                ", brokers=" + brokers +
                '}';
    }
}