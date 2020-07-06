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

import org.joyqueue.domain.Topic;
import org.joyqueue.event.EventType;
import org.joyqueue.event.MetaEvent;

/**
 * AddTopicEvent
 * author: gaohaoxiang
 * date: 2019/8/28
 */
public class UpdateTopicEvent extends MetaEvent {

    private Topic oldTopic;
    private Topic newTopic;

    public UpdateTopicEvent() {

    }

    public UpdateTopicEvent(Topic oldTopic, Topic newTopic) {
        this.oldTopic = oldTopic;
        this.newTopic = newTopic;
    }

    public UpdateTopicEvent(EventType eventType, Topic oldTopic, Topic newTopic) {
        super(eventType);
        this.oldTopic = oldTopic;
        this.newTopic = newTopic;
    }

    public Topic getOldTopic() {
        return oldTopic;
    }

    public void setOldTopic(Topic oldTopic) {
        this.oldTopic = oldTopic;
    }

    public Topic getNewTopic() {
        return newTopic;
    }

    public void setNewTopic(Topic newTopic) {
        this.newTopic = newTopic;
    }

    @Override
    public String getTypeName() {
        return EventType.UPDATE_TOPIC.name();
    }
}