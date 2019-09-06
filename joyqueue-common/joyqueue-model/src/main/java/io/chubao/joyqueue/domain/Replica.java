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
package io.chubao.joyqueue.domain;

import java.io.Serializable;

/**
 * @author wylixiaobin
 * Date: 2018/8/20
 */
public class Replica implements Serializable {
    private String id;
    /**
     * 主题
     */
    protected TopicName topic;
    /**
     * partition 分组
     */
    protected int group;
    /**
     * Broker ID
     */
    protected int brokerId;

    public Replica() {

    }

    public Replica(String id, TopicName topic, int group, int brokerId) {
        this.id = id;
        this.topic = topic;
        this.group = group;
        this.brokerId = brokerId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TopicName getTopic() {
        return topic;
    }

    public void setTopic(TopicName topic) {
        this.topic = topic;
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
