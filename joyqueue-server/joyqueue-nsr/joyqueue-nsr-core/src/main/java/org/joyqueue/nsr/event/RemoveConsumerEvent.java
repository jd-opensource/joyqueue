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

import org.joyqueue.domain.Consumer;
import org.joyqueue.domain.TopicName;
import org.joyqueue.event.EventType;
import org.joyqueue.event.MetaEvent;

/**
 * RemoveConsumerEvent
 * author: gaohaoxiang
 * date: 2019/8/29
 */
public class RemoveConsumerEvent extends MetaEvent {

    private TopicName topic;
    private Consumer consumer;

    public RemoveConsumerEvent() {

    }

    public RemoveConsumerEvent(TopicName topic, Consumer consumer) {
        this.topic = topic;
        this.consumer = consumer;
    }

    public RemoveConsumerEvent(EventType eventType, TopicName topic, Consumer consumer) {
        super(eventType);
        this.topic = topic;
        this.consumer = consumer;
    }

    public TopicName getTopic() {
        return topic;
    }

    public void setTopic(TopicName topic) {
        this.topic = topic;
    }

    public Consumer getConsumer() {
        return consumer;
    }

    public void setConsumer(Consumer consumer) {
        this.consumer = consumer;
    }

    @Override
    public String getTypeName() {
        return EventType.REMOVE_CONSUMER.name();
    }
}