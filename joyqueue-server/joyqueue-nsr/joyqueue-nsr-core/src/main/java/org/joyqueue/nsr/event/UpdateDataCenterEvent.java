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

import org.joyqueue.domain.DataCenter;
import org.joyqueue.event.EventType;
import org.joyqueue.event.MetaEvent;

/**
 * UpdateDataCenterEvent
 * author: gaohaoxiang
 * date: 2019/9/3
 */
public class UpdateDataCenterEvent extends MetaEvent {

    private DataCenter oldDataCenter;
    private DataCenter newDataCenter;

    public UpdateDataCenterEvent() {

    }

    public UpdateDataCenterEvent(DataCenter oldDataCenter, DataCenter newDataCenter) {
        this.oldDataCenter = oldDataCenter;
        this.newDataCenter = newDataCenter;
    }

    public UpdateDataCenterEvent(EventType eventType, DataCenter oldDataCenter, DataCenter newDataCenter) {
        super(eventType);
        this.oldDataCenter = oldDataCenter;
        this.newDataCenter = newDataCenter;
    }

    public DataCenter getOldDataCenter() {
        return oldDataCenter;
    }

    public void setOldDataCenter(DataCenter oldDataCenter) {
        this.oldDataCenter = oldDataCenter;
    }

    public DataCenter getNewDataCenter() {
        return newDataCenter;
    }

    public void setNewDataCenter(DataCenter newDataCenter) {
        this.newDataCenter = newDataCenter;
    }

    @Override
    public String getTypeName() {
        return EventType.UPDATE_DATACENTER.name();
    }
}