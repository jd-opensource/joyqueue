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
 * UpdateConsumerEvent
 * author: gaohaoxiang
 * date: 2019/8/29
 */
public class UpdateConsumerEvent extends MetaEvent {

    private TopicName topic;
    private Consumer oldConsumer;
    private Consumer newConsumer;

    public UpdateConsumerEvent() {

    }

    public UpdateConsumerEvent(TopicName topic, Consumer oldConsumer, Consumer newConsumer) {
        this.topic = topic;
        this.oldConsumer = oldConsumer;
        this.newConsumer = newConsumer;
    }

    public UpdateConsumerEvent(EventType eventType, TopicName topic, Consumer oldConsumer, Consumer newConsumer) {
        super(eventType);
        this.topic = topic;
        this.oldConsumer = oldConsumer;
        this.newConsumer = newConsumer;
    }

    public TopicName getTopic() {
        return topic;
    }

    public void setTopic(TopicName topic) {
        this.topic = topic;
    }

    public Consumer getOldConsumer() {
        return oldConsumer;
    }

    public void setOldConsumer(Consumer oldConsumer) {
        this.oldConsumer = oldConsumer;
    }

    public Consumer getNewConsumer() {
        return newConsumer;
    }

    public void setNewConsumer(Consumer newConsumer) {
        this.newConsumer = newConsumer;
    }

    @Override
    public String getTypeName() {
        return EventType.UPDATE_CONSUMER.name();
    }
}