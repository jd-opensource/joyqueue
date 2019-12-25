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
package org.joyqueue.broker.kafka.coordinator.group.domain;

import org.joyqueue.broker.kafka.model.TopicAndPartition;

/**
 * GroupTopicPartition
 *
 * @author luoruiheng
 * @since 1/18/18
 */
public class GroupTopicPartition {

    private String groupId;
    private TopicAndPartition topicAndPartition;

    public GroupTopicPartition(String groupId, String topic, int partition) {
        this.groupId = groupId;
        this.topicAndPartition = new TopicAndPartition(topic, partition);
    }

    public GroupTopicPartition(String groupId, TopicAndPartition topicAndPartition) {
        this.groupId = groupId;
        this.topicAndPartition = topicAndPartition;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public TopicAndPartition getTopicAndPartition() {
        return topicAndPartition;
    }

    public void setTopicAndPartition(TopicAndPartition topicAndPartition) {
        this.topicAndPartition = topicAndPartition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GroupTopicPartition that = (GroupTopicPartition) o;

        if (!groupId.equals(that.groupId)) return false;
        return topicAndPartition.equals(that.topicAndPartition);
    }

    @Override
    public int hashCode() {
        int result = groupId.hashCode();
        result = 31 * result + topicAndPartition.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "GroupTopicPartition{" +
                "groupId='" + groupId + '\'' +
                ", topicAndPartition=" + topicAndPartition +
                '}';
    }
}
