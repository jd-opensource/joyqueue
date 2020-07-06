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
package org.joyqueue.broker.kafka;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * KafkaCommandType
 *
 * author: gaohaoxiang
 * date: 2018/11/5
 */
public enum KafkaCommandType {

    // 发送消息
    PRODUCE(0, 0, 7),

    // 取消息
    FETCH(1, 2, 6),

    // 取offsets
    LIST_OFFSETS(2, 0, 3),

    // 取元数据
    METADATA(3, 0, 4),

    // 提交commit
    OFFSET_COMMIT(8, 0, 4),

    // 取offset
    OFFSET_FETCH(9, 0, 4),

    // 取消费者元数据
    FIND_COORDINATOR(10, 0, 2),

    // 加入组
    JOIN_GROUP(11, 0, 3),

    // 心跳
    HEARTBEAT(12, 0, 2),

    // 离开组
    LEAVE_GROUP(13, 0, 2),

    // 同步组
    SYNC_GROUP(14, 0, 2),

    // 描述组
    DESCRIBE_GROUP(15, 0, 2),

    // API VERSION
    API_VERSIONS(18, 0, 3),

    // 事务
    INIT_PRODUCER_ID(22, 0, 0),

    ADD_PARTITIONS_TO_TXN(24, 0, 1),

    ADD_OFFSETS_TO_TXN(25, 0, 1),

    END_TXN(26, 0, 1),

    TXN_OFFSET_COMMIT(28, 0, 2),

    // SASL
    SASL_AUTHENTICATE(36, 0, 1),

    SASL_HANDSHAKE(17, 0, 1),

    // 自定义命令
    CUSTOMER_COMMAND(100, false),

    ;

    private static final short MIN_VERSION = 0;
    private static final short MAX_VERSION = 0;
    private static final Map<Short, KafkaCommandType> TYPES;

    static {
        Map<Short, KafkaCommandType> types = Maps.newHashMap();
        for (KafkaCommandType kafkaCommandType : KafkaCommandType.values()) {
            types.put(kafkaCommandType.getCode(), kafkaCommandType);
        }
        TYPES = types;
    }

    private short code;
    private short minVersion;
    private short maxVersion;
    private boolean export;

    KafkaCommandType(int code) {
        this(code, true);
    }

    KafkaCommandType(int code, boolean export) {
        this(code, MIN_VERSION, MAX_VERSION, export);
    }

    KafkaCommandType(int code, int minVersion, int maxVersion) {
        this(code, minVersion, maxVersion, true);
    }

    KafkaCommandType(int code, int minVersion, int maxVersion, boolean export) {
        this.code = (short) code;
        this.minVersion = (short) minVersion;
        this.maxVersion = (short) maxVersion;
        this.export = export;
    }

    public static KafkaCommandType valueOf(short type) {
        return TYPES.get(type);
    }

    public static boolean contains(short type) {
        return TYPES.containsKey(type);
    }

    public short getCode() {
        return code;
    }

    public short getMinVersion() {
        return minVersion;
    }

    public short getMaxVersion() {
        return maxVersion;
    }

    public boolean isExport() {
        return export;
    }
}
