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
package org.joyqueue.broker.cluster.event;

import org.joyqueue.domain.TopicConfig;
import org.joyqueue.domain.TopicName;
import org.joyqueue.event.EventType;
import org.joyqueue.event.MetaEvent;

import java.util.Map;

/**
 * CompensateEvent
 * author: gaohaoxiang
 * date: 2019/10/18
 */
public class CompensateEvent extends MetaEvent {

    private Map<TopicName, TopicConfig> topics;

    public CompensateEvent() {

    }

    public CompensateEvent(Map<TopicName, TopicConfig> topics) {
        this.topics = topics;
    }

    public void setTopics(Map<TopicName, TopicConfig> topics) {
        this.topics = topics;
    }

    public Map<TopicName, TopicConfig> getTopics() {
        return topics;
    }

    @Override
    public String getTypeName() {
        return EventType.COMPENSATE.name();
    }
}