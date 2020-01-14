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

import org.joyqueue.domain.Config;
import org.joyqueue.event.EventType;
import org.joyqueue.event.MetaEvent;

/**
 * UpdateConfigEvent
 * author: gaohaoxiang
 * date: 2019/9/3
 */
public class UpdateConfigEvent extends MetaEvent {

    private Config oldConfig;
    private Config newConfig;

    public UpdateConfigEvent() {

    }

    public UpdateConfigEvent(Config oldConfig, Config newConfig) {
        this.oldConfig = oldConfig;
        this.newConfig = newConfig;
    }

    public UpdateConfigEvent(EventType eventType, Config oldConfig, Config newConfig) {
        super(eventType);
        this.oldConfig = oldConfig;
        this.newConfig = newConfig;
    }

    public Config getOldConfig() {
        return oldConfig;
    }

    public void setOldConfig(Config oldConfig) {
        this.oldConfig = oldConfig;
    }

    public Config getNewConfig() {
        return newConfig;
    }

    public void setNewConfig(Config newConfig) {
        this.newConfig = newConfig;
    }

    @Override
    public String getTypeName() {
        return EventType.UPDATE_CONFIG.name();
    }
}