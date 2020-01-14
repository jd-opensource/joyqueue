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
import org.joyqueue.domain.Topic;
import org.joyqueue.event.EventType;
import org.joyqueue.event.MetaEvent;

import java.util.List;

/**
 * RemoveTopicEvent
 * author: gaohaoxiang
 * date: 2019/8/28
 */
public class RemoveTopicEvent extends MetaEvent {

    private Topic topic;
    private List<PartitionGroup> partitionGroups;

    public RemoveTopicEvent() {

    }

    public RemoveTopicEvent(Topic topic, List<PartitionGroup> partitionGroups) {
        this.topic = topic;
        this.partitionGroups = partitionGroups;
    }

    public RemoveTopicEvent(EventType eventType, Topic topic, List<PartitionGroup> partitionGroups) {
        super(eventType);
        this.topic = topic;
        this.partitionGroups = partitionGroups;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public List<PartitionGroup> getPartitionGroups() {
        return partitionGroups;
    }

    public void setPartitionGroups(List<PartitionGroup> partitionGroups) {
        this.partitionGroups = partitionGroups;
    }

    @Override
    public String getTypeName() {
        return EventType.REMOVE_TOPIC.name();
    }
}