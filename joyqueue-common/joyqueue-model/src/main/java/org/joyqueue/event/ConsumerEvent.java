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
public class ConsumerEvent extends MetaEvent {
    private TopicName topic;
    private String app;

    public ConsumerEvent() {
    }

    public ConsumerEvent(EventType type, TopicName topic, String app) {
        super(type);
        this.topic = topic;
        this.app = app;
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

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public static ConsumerEvent add(TopicName topic, String app) {
        return new ConsumerEvent(EventType.ADD_CONSUMER, topic, app);
    }

    public static ConsumerEvent update(TopicName topic, String app) {
        return new ConsumerEvent(EventType.UPDATE_CONSUMER, topic, app);
    }

    public static ConsumerEvent remove(TopicName topic, String app) {
        return new ConsumerEvent(EventType.REMOVE_CONSUMER, topic, app);
    }

    @Override
    public String toString() {
        return "ConsumerEvent{" +
                "topic='" + topic + '\'' +
                ", app='" + app + '\'' +
                '}';
    }
}
