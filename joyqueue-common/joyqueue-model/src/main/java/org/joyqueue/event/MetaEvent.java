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

import java.io.Serializable;

/**
 * @author lixiaobin6
 */
public abstract class MetaEvent implements Serializable {
    protected EventType eventType;

    public MetaEvent() {
    }

    public MetaEvent(EventType eventType) {
        this.eventType = eventType;
    }

    public EventType getEventType() {
        if (eventType == null) {
            eventType = EventType.valueOf(getTypeName());
        }
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public abstract String getTypeName();
    @Override
    public String toString() {
        return "MetaEvent{" +
                "eventType=" + eventType +
                '}';
    }
}
