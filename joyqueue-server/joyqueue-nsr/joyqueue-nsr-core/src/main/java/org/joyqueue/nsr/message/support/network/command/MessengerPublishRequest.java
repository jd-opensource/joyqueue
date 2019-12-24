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
package org.joyqueue.nsr.message.support.network.command;

import org.joyqueue.event.MetaEvent;
import org.joyqueue.network.transport.command.JoyQueuePayload;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.nsr.network.command.NsrCommandType;

/**
 * MessengerPublishRequest
 * author: gaohaoxiang
 * date: 2019/8/27
 */
public class MessengerPublishRequest extends JoyQueuePayload implements Type {

    private String type;
    private String classType;
    private MetaEvent event;

    public MessengerPublishRequest() {

    }

    public MessengerPublishRequest(MetaEvent event) {
        this.type = event.getTypeName();
        this.classType = event.getClass().getName();
        this.event = event;
    }

    public MessengerPublishRequest(String type, String classType, MetaEvent event) {
        this.type = type;
        this.classType = classType;
        this.event = event;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getClassType() {
        return classType;
    }

    public void setClassType(String classType) {
        this.classType = classType;
    }

    public MetaEvent getEvent() {
        return event;
    }

    public void setEvent(MetaEvent event) {
        this.event = event;
    }

    @Override
    public int type() {
        return NsrCommandType.NSR_MESSENGER_PUBLISH_REQUEST;
    }
}
