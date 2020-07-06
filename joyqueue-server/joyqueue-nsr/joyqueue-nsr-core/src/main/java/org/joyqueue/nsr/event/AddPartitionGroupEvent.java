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
package org.joyqueue.nsr.event;

import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.TopicName;
import org.joyqueue.event.EventType;
import org.joyqueue.event.MetaEvent;

/**
 * AddPartitionGroupEvent
 * author: gaohaoxiang
 * date: 2019/8/28
 */
public class AddPartitionGroupEvent extends MetaEvent {

    private TopicName topic;
    private PartitionGroup partitionGroup;

    public AddPartitionGroupEvent() {

    }

    public AddPartitionGroupEvent(TopicName topic, PartitionGroup partitionGroup) {
        this.topic = topic;
        this.partitionGroup = partitionGroup;
    }

    public AddPartitionGroupEvent(EventType eventType, TopicName topic, PartitionGroup partitionGroup) {
        super(eventType);
        this.topic = topic;
        this.partitionGroup = partitionGroup;
    }

    public TopicName getTopic() {
        return topic;
    }

    public void setTopic(TopicName topic) {
        this.topic = topic;
    }

    public void setPartitionGroup(PartitionGroup partitionGroup) {
        this.partitionGroup = partitionGroup;
    }

    public PartitionGroup getPartitionGroup() {
        return partitionGroup;
    }

    @Override
    public String getTypeName() {
        return EventType.ADD_PARTITION_GROUP.name();
    }
}