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
package org.joyqueue.domain;

import java.util.List;


/**
 *
 * 2019/01/23
 * @author  wangjin18
 *
 **/
public class CoordinatorDetail {
    private TopicName topic;
    private int partitionGroup;
    private Broker current;   // coordinator broker
    private List<Broker> replicas;

    public CoordinatorDetail(){

    }
    public CoordinatorDetail(TopicName topic, int partitionGroup, Broker current, List<Broker> replicas) {
        this.topic = topic;
        this.partitionGroup = partitionGroup;
        this.current = current;
        this.replicas = replicas;
    }

    public TopicName getTopic() {
        return topic;
    }

    public void setTopic(TopicName topic) {
        this.topic = topic;
    }

    public void setPartitionGroup(int partitionGroup) {
        this.partitionGroup = partitionGroup;
    }

    public int getPartitionGroup() {
        return partitionGroup;
    }

    public Broker getCurrent() {
        return current;
    }

    public void setCurrent(Broker current) {
        this.current = current;
    }

    public List<Broker> getReplicas() {
        return replicas;
    }

    public void setReplicas(List<Broker> replicas) {
        this.replicas = replicas;
    }
}
