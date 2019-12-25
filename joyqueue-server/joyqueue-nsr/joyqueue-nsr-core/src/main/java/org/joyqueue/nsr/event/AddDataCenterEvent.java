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
 * AddDataCenterEvent
 * author: gaohaoxiang
 * date: 2019/9/3
 */
public class AddDataCenterEvent extends MetaEvent {

    private DataCenter dataCenter;

    public AddDataCenterEvent() {

    }

    public AddDataCenterEvent(DataCenter dataCenter) {
        this.dataCenter = dataCenter;
    }

    public AddDataCenterEvent(EventType eventType, DataCenter dataCenter) {
        super(eventType);
        this.dataCenter = dataCenter;
    }

    public DataCenter getDataCenter() {
        return dataCenter;
    }

    public void setDataCenter(DataCenter dataCenter) {
        this.dataCenter = dataCenter;
    }

    @Override
    public String getTypeName() {
        return EventType.ADD_DATACENTER.name();
    }
}