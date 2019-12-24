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

public class PartitionGroupQuery implements Query {
    /**
     * 主题
     */
    private String topic;
    /**
     * 命名空间
     */
    private String namespace;
    /**
     * 分组
     */
    private int group = -1;

    private String keyword;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
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


    public PartitionGroupQuery(String topic, String namespace) {
        this.topic = topic;
        this.namespace = namespace;
    }

    public PartitionGroupQuery(String topic, String namespace, int group) {
        this.topic = topic;
        this.namespace = namespace;
        this.group = group;
    }

    public PartitionGroupQuery() {
    }
}
