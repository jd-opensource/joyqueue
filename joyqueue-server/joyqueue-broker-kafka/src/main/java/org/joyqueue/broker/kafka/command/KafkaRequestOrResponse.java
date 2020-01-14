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
package org.joyqueue.broker.kafka.command;

import org.joyqueue.network.transport.command.Direction;
import org.joyqueue.network.transport.command.Payload;
import org.joyqueue.network.transport.command.Type;

/**
 * Created by zhuduohui on 2018/8/30.
 */
public abstract class KafkaRequestOrResponse implements Payload, Type {

    // common field
    private short version;
    private int correlationId;
    private String clientId;

    private Direction direction;
    private int throttleTimeMs;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public short getVersion() {
        return version;
    }

    public void setVersion(short version) {
        this.version = version;
    }

    public int getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(int correlationId) {
        this.correlationId = correlationId;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void setThrottleTimeMs(int throttleTimeMs) {
        this.throttleTimeMs = throttleTimeMs;
    }

    public int getThrottleTimeMs() {
        return throttleTimeMs;
    }
}

