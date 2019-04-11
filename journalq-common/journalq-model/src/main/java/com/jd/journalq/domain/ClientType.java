/**
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
package com.jd.journalq.domain;

public enum ClientType {
    JMQ((byte) 0, "jmq"),

    KAFKA((byte) 1, "kafka"),

    MQTT((byte) 2, "mqtt"),

    OTHERS((byte) 10, "others");

    private byte value;
    private String name;

    ClientType(byte value, String name) {
        this.value = value;
        this.name = name;
    }

    public byte value() {
        return value;
    }

    public String getName() {
        return name;
    }

    public static ClientType valueOf(int value) {
        for (ClientType type : ClientType.values()) {
            if (value == type.value) {
                return type;
            }
        }
        return OTHERS;
    }
}
