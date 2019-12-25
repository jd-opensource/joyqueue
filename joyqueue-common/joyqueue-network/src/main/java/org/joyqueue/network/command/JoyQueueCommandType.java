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

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * JoyQueueCommandType
 *
 * author: gaohaoxiang
 * date: 2018/11/28
 */
public enum JoyQueueCommandType {

    // 连接相关
    ADD_CONNECTION_REQUEST(1),
    ADD_CONNECTION_RESPONSE(-1),
    REMOVE_CONNECTION_REQUEST(2),
    ADD_CONSUMER_REQUEST(3),
    ADD_CONSUMER_RESPONSE(-3),
    REMOVE_CONSUMER_REQUEST(4),
    ADD_PRODUCER_REQUEST(5),
    ADD_PRODUCER_RESPONSE(-5),
    REMOVE_PRODUCER_REQUEST(6),
    HEARTBEAT_REQUEST(7),
    FETCH_HEALTH_REQUEST(8),
    FETCH_HEALTH_RESPONSE(-8),

    // 集群相关
    FETCH_CLUSTER_REQUEST(10),
    FETCH_CLUSTER_RESPONSE(-10),

    // 协调者相关
    FIND_COORDINATOR_REQUEST(20),
    FIND_COORDINATOR_RESPONSE(-20),
    FETCH_ASSIGNED_PARTITION_REQUEST(21),
    FETCH_ASSIGNED_PARTITION_RESPONSE(-21),

    // 消费相关
    FETCH_TOPIC_MESSAGE_REQUEST(30),
    FETCH_TOPIC_MESSAGE_RESPONSE(-30),
    FETCH_PARTITION_MESSAGE_REQUEST(31),
    FETCH_PARTITION_MESSAGE_RESPONSE(-31),
    COMMIT_ACK_REQUEST(32),
    COMMIT_ACK_RESPONSE(-32),
    COMMIT_ACK_INDEX_REQUEST(33),
    COMMIT_ACK_INDEX_RESPONSE(-33),
//    FETCH_ACK_INDEX_REQUEST(34),
//    FETCH_ACK_INDEX_RESPONSE(-34),
    FETCH_INDEX_REQUEST(35),
    FETCH_INDEX_RESPONSE(-35),

    // 生产相关
    PRODUCE_MESSAGE_REQUEST(50),
    PRODUCE_MESSAGE_RESPONSE(-50),
    PRODUCE_MESSAGE_PREPARE_REQUEST(51),
    PRODUCE_MESSAGE_PREPARE_RESPONSE(-51),
    PRODUCE_MESSAGE_COMMIT_REQUEST(52),
    PRODUCE_MESSAGE_COMMIT_RESPONSE(-52),
    PRODUCE_MESSAGE_ROLLBACK_REQUEST(53),
    PRODUCE_MESSAGE_ROLLBACK_RESPONSE(-53),
    FETCH_PRODUCE_FEEDBACK_REQUEST(54),
    FETCH_PRODUCE_FEEDBACK_RESPONSE(-54),

    // mqtt使用
    MQTT_SUBSCRIBE(100),
    MQTT_SUBSCRIBE_ACK(-100),
    MQTT_UNSUBSCRIBE(101),
    MQTT_GET_TOPICS(102),
    MQTT_GET_TOPICS_ACK(-102),
    MQTT_AUTHORIZATION(103),

    ;

    private static final Map<Byte, JoyQueueCommandType> TYPES;

    static {
        Map<Byte, JoyQueueCommandType> types = Maps.newHashMap();
        for (JoyQueueCommandType commandType : JoyQueueCommandType.values()) {
            types.put(commandType.getCode(), commandType);
        }
        TYPES = types;
    }

    private byte code;

    JoyQueueCommandType(int code) {
        this.code = (byte) code;
    }

    public byte getCode() {
        return code;
    }

    public static boolean contains(byte code) {
        return TYPES.containsKey(code);
    }

    public static JoyQueueCommandType valueOf(byte code) {
        return TYPES.get(code);
    }
}