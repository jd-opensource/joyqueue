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

import org.joyqueue.domain.Broker;

@Deprecated
public class BrokerEvent extends MetaEvent {
    private Broker broker;

    public BrokerEvent() {
    }

    private BrokerEvent(EventType type, Broker broker) {
        super(type);
        this.broker = broker;
    }
    @Override
    public String getTypeName() {
        return getClass().getTypeName();
    }
    public Broker getBroker() {
        return broker;
    }

    public void setBroker(Broker broker) {
        this.broker = broker;
    }

    public static BrokerEvent event(Broker broker) {
        return new BrokerEvent(EventType.UPDATE_BROKER, broker);
    }

    @Override
    public String toString() {
        return "BrokerEvent{" +
                "broker=" + broker +
                '}';
    }
}
