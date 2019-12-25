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
package org.joyqueue.nsr.model;

import org.joyqueue.model.Query;

public class ReplicaQuery implements Query {
    private String topic;
    private String namespace;
    private int group = -1;
    private int brokerId;

    public ReplicaQuery() {
    }

    public ReplicaQuery(String topic, String namespace, int group, int brokerId) {
        this.topic = topic;
        this.namespace = namespace;
        this.group = group;
        this.brokerId = brokerId;
    }

    public ReplicaQuery(String topic, String namespace, int group) {
        this(topic, namespace, group, 0);
    }


    public ReplicaQuery(int brokerId) {
        this.brokerId = brokerId;
    }

    public ReplicaQuery(String topic, String namespace) {
        this.topic = topic;
        this.namespace = namespace;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public int getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(int brokerId) {
        this.brokerId = brokerId;
    }
}
