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
public class ProducerEvent extends MetaEvent {
    private TopicName topic;
    private String app;

    public ProducerEvent() {
    }

    public ProducerEvent(EventType type,TopicName topic, String app) {
        super(type);
        this.app = app;
        this.topic = topic;
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

    public void setApp(String app) {
        this.app = app;
    }

    public String getApp() {
        return app;
    }

    public static ProducerEvent add(TopicName topic, String app) {
        return new ProducerEvent(EventType.ADD_PRODUCER, topic, app);
    }

    public static ProducerEvent update(TopicName topic, String app) {
        return new ProducerEvent(EventType.UPDATE_PRODUCER, topic, app);
    }

    public static ProducerEvent remove(TopicName topic, String app) {
        return new ProducerEvent(EventType.REMOVE_PRODUCER, topic, app);
    }

    @Override
    public String toString() {
        return "ProducerEvent{" +
                "topic='" + topic + '\'' +
                ", app='" + app + '\'' +
                '}';
    }
}
