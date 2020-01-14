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
import java.util.Arrays;

/**
 * @author majun8
 */
public class MqttStoredMessage implements Serializable {
    private static final long serialVersionUID = -1;

    final MqttQoS qos;
    final byte[] payload;
    final String topic;
    private boolean retained;
    private String clientID;
    private byte[] clientAddress;
    private short partition;
    private long index;

    public MqttStoredMessage(byte[] message, MqttQoS qos, String topic) {
        this.qos = qos;
        this.payload = message;
        this.topic = topic;
    }

    public MqttQoS getQos() {
        return qos;
    }

    public byte[] getPayload() {
        return payload;
    }

    public String getTopic() {
        return topic;
    }

    public boolean isRetained() {
        return retained;
    }

    public void setRetained(boolean retained) {
        this.retained = retained;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public byte[] getClientAddress() {
        return clientAddress;
    }

    public void setClientAddress(byte[] clientAddress) {
        this.clientAddress = clientAddress;
    }

    public short getPartition() {
        return partition;
    }

    public void setPartition(short partition) {
        this.partition = partition;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return "MqttStoredMessage{" +
                "qos=" + qos +
                ", payload=" + Arrays.toString(payload) +
                ", topic='" + topic + '\'' +
                ", retained=" + retained +
                ", clientID='" + clientID + '\'' +
                ", clientAddress=" + Arrays.toString(clientAddress) +
                ", partition=" + partition +
                ", index=" + index +
                '}';
    }
}
