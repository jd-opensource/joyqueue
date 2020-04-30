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
package org.joyqueue.broker.coordinator.config;

import org.joyqueue.toolkit.config.PropertyDef;

/**
 * CoordinatorConfigKey
 *
 * author: gaohaoxiang
 * date: 2018/12/4
 */
public enum CoordinatorConfigKey implements PropertyDef {

    // 协调者作用域
    GROUP_NAMESPACE("coordinator.group.namespace", "", Type.STRING),

    // 协调者主题
    GROUP_TOPIC_CODE("coordinator.group.topic.code", "__group_coordinators", Type.STRING),
    // 协调者主题分区
    GROUP_TOPIC_PARTITIONS("coordinator.group.topic.partitions", (short) 10, Type.SHORT),
    // 协调者过期时间
    GROUP_EXPIRE_TIME("coordinator.group.expire.time", 1000 * 60 * 60 * 1, Type.INT),

    // 事务协调者主题
    TRANSACTION_TOPIC_CODE("coordinator.transaction.topic.code", "__transaction_coordinators", Type.STRING),
    // 事务协调者主题分区
    TRANSACTION_TOPIC_PARTITIONS("coordinator.transaction.topic.partitions", (short) 10, Type.SHORT),
    // 事务日志app
    TRANSACTION_LOG_APP("coordinator.transaction.log.app", "__transaction_log", Type.STRING),
    // 事务过期时间
    TRANSACTION_EXPIRE_TIME("coordinator.transaction.expire.time", 1000 * 60 * 60 * 1, Type.INT),
    // 最多事务数
    TRANSACTION_MAX_NUM("coordinator.transaction.max.num", 1024 * 10, Type.INT),

    // session同步超时
    SESSION_SYNC_TIMEOUT("coordinator.session.sync.timeout", 1000 * 3, Type.INT),
    // session缓存时间
    SESSION_EXPIRE_TIME("coordinator.session.expire.time", 1000 * 60 * 10, Type.INT),

    ;

    public static final String TRANSPORT_KEY_PREFIX = "coordinator.";

    private String name;
    private Object value;
    private Type type;

    CoordinatorConfigKey(String name, Object value, Type type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    public Type getType() {
        return type;
    }
}