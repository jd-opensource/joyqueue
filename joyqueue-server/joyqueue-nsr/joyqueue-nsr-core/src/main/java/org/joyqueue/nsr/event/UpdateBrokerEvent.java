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

import org.joyqueue.domain.Broker;
import org.joyqueue.event.EventType;
import org.joyqueue.event.MetaEvent;

/**
 * UpdateBrokerEvent
 * author: gaohaoxiang
 * date: 2019/8/29
 */
public class UpdateBrokerEvent extends MetaEvent {

    private Broker oldBroker;
    private Broker newBroker;

    public UpdateBrokerEvent(Broker oldBroker, Broker newBroker) {
        this.oldBroker = oldBroker;
        this.newBroker = newBroker;
    }

    public UpdateBrokerEvent(EventType eventType, Broker oldBroker, Broker newBroker) {
        super(eventType);
        this.oldBroker = oldBroker;
        this.newBroker = newBroker;
    }

    public Broker getOldBroker() {
        return oldBroker;
    }

    public void setOldBroker(Broker oldBroker) {
        this.oldBroker = oldBroker;
    }

    public Broker getNewBroker() {
        return newBroker;
    }

    public void setNewBroker(Broker newBroker) {
        this.newBroker = newBroker;
    }

    @Override
    public String getTypeName() {
        return EventType.UPDATE_BROKER.name();
    }
}