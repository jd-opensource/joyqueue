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
package io.chubao.joyqueue.nsr.nameservice;

import io.chubao.joyqueue.event.MetaEvent;
import io.chubao.joyqueue.event.NameServerEvent;
import io.chubao.joyqueue.nsr.message.MessageListener;
import io.chubao.joyqueue.toolkit.concurrent.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NameServiceEventAdapter
 * author: gaohaoxiang
 * date: 2019/8/28
 */
public class NameServiceEventListenerAdapter implements MessageListener<MetaEvent> {

    protected static final Logger logger = LoggerFactory.getLogger(NameServiceEventListenerAdapter.class);

    private EventBus<NameServerEvent> eventBus;

    public NameServiceEventListenerAdapter(EventBus<NameServerEvent> eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void onEvent(MetaEvent event) {
        NameServerEvent nameServerEvent = new NameServerEvent();
        nameServerEvent.setMetaEvent(event);
        nameServerEvent.setEventType(event.getEventType());
        eventBus.inform(nameServerEvent);
    }
}