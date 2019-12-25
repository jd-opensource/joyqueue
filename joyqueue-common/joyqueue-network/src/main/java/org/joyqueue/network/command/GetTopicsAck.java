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
package org.joyqueue.network.command;

import org.joyqueue.network.transport.command.Payload;
import org.joyqueue.network.transport.command.Types;

import java.util.Set;

/**
 * @author wylixiaobin
 * Date: 2018/10/17
 */
public class GetTopicsAck implements Payload, Types {
    private Set<String> topics;

    public GetTopicsAck topics(Set<String> topics){
        this.topics = topics;
        return this;
    }
    public Set<String> getTopics() {
        return topics;
    }

    @Override
    public int[] types() {
        return new int[]{CommandType.GET_TOPICS_ACK, JoyQueueCommandType.MQTT_GET_TOPICS_ACK.getCode()};
    }
}
