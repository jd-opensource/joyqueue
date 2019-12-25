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
 * AddBrokerEvent
 * author: gaohaoxiang
 * date: 2019/8/29
 */
public class AddBrokerEvent extends MetaEvent {

    private Broker broker;

    public AddBrokerEvent() {

    }

    public AddBrokerEvent(Broker broker) {
        this.broker = broker;
    }

    public AddBrokerEvent(EventType eventType, Broker broker) {
        super(eventType);
        this.broker = broker;
    }

    public Broker getBroker() {
        return broker;
    }

    public void setBroker(Broker broker) {
        this.broker = broker;
    }

    @Override
    public String getTypeName() {
        return EventType.ADD_BROKER.name();
    }
}