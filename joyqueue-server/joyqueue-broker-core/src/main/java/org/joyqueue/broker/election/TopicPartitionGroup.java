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
package org.joyqueue.broker.election;


/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 227018/8/
 */
public class TopicPartitionGroup {
    public String topic;
    public int partitionGroupId;

    public TopicPartitionGroup() {}

    public TopicPartitionGroup(String topic, int partitionGroupId) {
        this.topic = topic;
        this.partitionGroupId = partitionGroupId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getPartitionGroupId() {
        return partitionGroupId;
    }

    @Override
    public int hashCode() {
        return topic.hashCode() + partitionGroupId;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof TopicPartitionGroup)) {
            return false;
        }

        TopicPartitionGroup topicPartitionGroup = (TopicPartitionGroup)object;

        return topic.equals(topicPartitionGroup.getTopic()) &&
                partitionGroupId == topicPartitionGroup.getPartitionGroupId();
    }

    @Override
    public String toString() {
        return topic + "-" + partitionGroupId;
    }
}
