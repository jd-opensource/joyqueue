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
package org.joyqueue.broker.mqtt.message;

import io.netty.handler.codec.mqtt.MqttQoS;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * @author majun8
 */
public final class WillMessage implements Serializable {
    private static final long serialVersionUID = -1L;

    private final String topic;
    private final ByteBuffer payload;
    private final boolean retained;
    private final MqttQoS qos;

    public WillMessage(String topic, ByteBuffer payload, boolean retained, MqttQoS qos) {
        this.topic = topic;
        this.payload = payload;
        this.retained = retained;
        this.qos = qos;
    }

    public String getTopic() {
        return topic;
    }

    public ByteBuffer getPayload() {
        return payload;
    }

    public boolean isRetained() {
        return retained;
    }

    public MqttQoS getQos() {
        return qos;
    }
}
