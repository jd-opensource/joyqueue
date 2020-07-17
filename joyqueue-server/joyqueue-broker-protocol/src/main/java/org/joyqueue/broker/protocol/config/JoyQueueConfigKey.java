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
package org.joyqueue.broker.protocol.config;

import org.joyqueue.toolkit.config.PropertyDef;

/**
 * JoyQueueConfigKey
 *
 * author: gaohaoxiang
 * date: 2018/12/3
 */
public enum JoyQueueConfigKey implements PropertyDef {

    // 协调者分区分配类型
    COORDINATOR_PARTITION_ASSIGN_TYPE("joyqueue.coordinator.partition.assign.type", "PARTITION_GROUP_BALANCE", PropertyDef.Type.STRING),

    // 协调者分配超时溢出
    COORDINATOR_PARTITION_ASSIGN_TIMEOUT_OVERFLOW("joyqueue.coordinator.partition.assign.timeout.overflow", 1000 * 60 * 1, PropertyDef.Type.INT),

    // 协调者分区分配最小连接数
    COORDINATOR_PARTITION_ASSIGN_MIN_CONNECTIONS("joyqueue.coordinator.partition.assign.minConnections", 3, PropertyDef.Type.INT),

    // 生产最大超时
    PRODUCE_MAX_TIMEOUT("joyqueue.producer.max.timeout", 1000 * 3, PropertyDef.Type.INT),


    ;

    private String name;
    private Object value;
    private PropertyDef.Type type;

     JoyQueueConfigKey(String name, Object value, PropertyDef.Type type) {
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

    public PropertyDef.Type getType() {
        return type;
    }
}