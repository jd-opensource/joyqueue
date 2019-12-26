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
package org.joyqueue.event;

import org.joyqueue.domain.TopicName;

@Deprecated
public class PartitionGroupEvent extends MetaEvent {
    private TopicName topic;
    private Integer partitionGroup;

    public PartitionGroupEvent() {
    }

    private PartitionGroupEvent(EventType type, TopicName topic, Integer partitionGroup) {
        super(type);
        this.topic = topic;
        this.partitionGroup = partitionGroup;
    }

    @Override
    public String getTypeName() {
        return getClass().getTypeName();
    }
    public TopicName getTopic() {
        return topic;
    }

    public void setTopic(TopicName topic) {
        this.topic = topic;
    }

    public void setPartitionGroup(Integer partitionGroup) {
        this.partitionGroup = partitionGroup;
    }



    public Integer getPartitionGroup() {
        return partitionGroup;
    }

    public static PartitionGroupEvent add(TopicName topic, Integer partitionGroup) {
        return new PartitionGroupEvent(EventType.ADD_PARTITION_GROUP, topic, partitionGroup);
    }

    public static PartitionGroupEvent update(TopicName topic, Integer partitionGroup) {
        return new PartitionGroupEvent(EventType.UPDATE_PARTITION_GROUP, topic, partitionGroup);
    }

    public static PartitionGroupEvent remove(TopicName topic, Integer partitionGroup) {
        return new PartitionGroupEvent(EventType.REMOVE_PARTITION_GROUP, topic, partitionGroup);
    }

    @Override
    public String toString() {
        return "PartitionGroupEvent{" +
                "topic='" + topic + '\'' +
                ", partitionGroup=" + partitionGroup +
                '}';
    }
}
