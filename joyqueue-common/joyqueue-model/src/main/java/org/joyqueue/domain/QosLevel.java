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
package org.joyqueue.domain;


/**
 * 服务水平
 */
public enum QosLevel {


    /**
     * 写入Qos级别：
     * ONE_WAY: 客户端单向向Broker发送消息，无应答；
     * RECEIVE: Broker收到消息后应答；
     * PERSISTENCE：Broker将消息写入磁盘后应答；
     * REPLICATION：Broker将消息复制到集群大多数节点后应答，默认值；
     * ALL：REPLICATION and PERSISTENCE；
     */
    ONE_WAY(0),
    RECEIVE(1),
    PERSISTENCE(2),
    REPLICATION(3),
    ALL(4);

    private int value;

    QosLevel(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public static QosLevel valueOf(final int value) {
        switch (value) {
            case 0:
                return ONE_WAY;
            case 1:
                return RECEIVE;
            case 2:
                return PERSISTENCE;
            case 4:
                return ALL;
            default:
                return REPLICATION;
        }
    }
}
