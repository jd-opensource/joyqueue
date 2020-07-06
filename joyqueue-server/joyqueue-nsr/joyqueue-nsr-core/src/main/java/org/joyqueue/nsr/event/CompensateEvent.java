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

import org.joyqueue.event.EventType;
import org.joyqueue.event.MetaEvent;
import org.joyqueue.nsr.nameservice.AllMetadataCache;

/**
 * CompensateEvent
 * author: gaohaoxiang
 * date: 2019/10/18
 */
public class CompensateEvent extends MetaEvent {

    private AllMetadataCache oldCache;
    private AllMetadataCache newCache;

    public CompensateEvent() {

    }

    public CompensateEvent(AllMetadataCache oldCache, AllMetadataCache newCache) {
        this.oldCache = oldCache;
        this.newCache = newCache;
    }

    public AllMetadataCache getOldCache() {
        return oldCache;
    }

    public void setOldCache(AllMetadataCache oldCache) {
        this.oldCache = oldCache;
    }

    public AllMetadataCache getNewCache() {
        return newCache;
    }

    public void setNewCache(AllMetadataCache newCache) {
        this.newCache = newCache;
    }

    @Override
    public String getTypeName() {
        return EventType.COMPENSATE.name();
    }
}