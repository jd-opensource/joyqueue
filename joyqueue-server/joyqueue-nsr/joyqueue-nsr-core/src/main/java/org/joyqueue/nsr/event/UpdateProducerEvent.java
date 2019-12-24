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
 * UpdateProducerEvent
 * author: gaohaoxiang
 * date: 2019/8/29
 */
public class UpdateProducerEvent extends MetaEvent {

    private TopicName topic;
    private Producer oldProducer;
    private Producer newProducer;

    public UpdateProducerEvent() {

    }

    public UpdateProducerEvent(TopicName topic, Producer oldProducer, Producer newProducer) {
        this.topic = topic;
        this.oldProducer = oldProducer;
        this.newProducer = newProducer;
    }

    public UpdateProducerEvent(EventType eventType, TopicName topic, Producer oldProducer, Producer newProducer) {
        super(eventType);
        this.topic = topic;
        this.oldProducer = oldProducer;
        this.newProducer = newProducer;
    }

    public TopicName getTopic() {
        return topic;
    }

    public void setTopic(TopicName topic) {
        this.topic = topic;
    }

    public Producer getOldProducer() {
        return oldProducer;
    }

    public void setOldProducer(Producer oldProducer) {
        this.oldProducer = oldProducer;
    }

    public Producer getNewProducer() {
        return newProducer;
    }

    public void setNewProducer(Producer newProducer) {
        this.newProducer = newProducer;
    }

    @Override
    public String getTypeName() {
        return EventType.UPDATE_PRODUCER.name();
    }
}