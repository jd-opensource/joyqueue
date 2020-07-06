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

/**
 * 元数据变更通知事件
 * <p>
 * Created by chengzhiliang on 2018/8/31.
 */
public class NameServerEvent extends MetaEvent {
    public static final Integer BROKER_ID_ALL_BROKER = -1;
    protected Integer brokerId;
    protected MetaEvent metaEvent;

    public NameServerEvent() {
    }

    public NameServerEvent(MetaEvent event, Integer brokerId) {
        super(event.getEventType());
        this.brokerId = brokerId;
        this.metaEvent = event;
    }
    @Override
    public String getTypeName() {
        return getClass().getTypeName();
    }
    public void setBrokerId(Integer brokerId) {
        this.brokerId = brokerId;
    }

    public void setMetaEvent(MetaEvent metaEvent) {
        this.metaEvent = metaEvent;
    }

    public Integer getBrokerId() {
        return brokerId;
    }

    public MetaEvent getMetaEvent() {
        return metaEvent;
    }


    @Override
    public String toString() {
        return "NameServerEvent{" +
                "brokerId=" + brokerId +
                ", metaEvent=" + metaEvent +
                ", type=" + eventType +
                '}';
    }
}
