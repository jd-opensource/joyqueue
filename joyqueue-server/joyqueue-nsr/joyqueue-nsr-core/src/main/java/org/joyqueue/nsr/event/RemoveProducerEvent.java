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

import org.joyqueue.domain.Producer;
import org.joyqueue.domain.TopicName;
import org.joyqueue.event.EventType;
import org.joyqueue.event.MetaEvent;

/**
 * RemoveProducerEvent
 * author: gaohaoxiang
 * date: 2019/8/29
 */
public class RemoveProducerEvent extends MetaEvent {

    private TopicName topic;
    private Producer producer;

    public RemoveProducerEvent() {

    }

    public RemoveProducerEvent(TopicName topic, Producer producer) {
        this.topic = topic;
        this.producer = producer;
    }

    public RemoveProducerEvent(EventType eventType, TopicName topic, Producer producer) {
        super(eventType);
        this.topic = topic;
        this.producer = producer;
    }

    public TopicName getTopic() {
        return topic;
    }

    public void setTopic(TopicName topic) {
        this.topic = topic;
    }

    public Producer getProducer() {
        return producer;
    }

    public void setProducer(Producer producer) {
        this.producer = producer;
    }

    @Override
    public String getTypeName() {
        return EventType.REMOVE_PRODUCER.name();
    }
}