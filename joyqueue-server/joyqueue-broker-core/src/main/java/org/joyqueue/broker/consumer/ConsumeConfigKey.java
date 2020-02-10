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
package org.joyqueue.broker.consumer;

import org.joyqueue.toolkit.config.PropertyDef;

/**
 * @author chengzhiliang on 2018/10/22.
 */
public enum ConsumeConfigKey implements PropertyDef {

    // 重试随机范围
    RETRY_RANDOM_BOUND("retry.random.bound", 1000, Type.INT),
    RETRY_RANDOM_BOUND_TOPIC_PREFIX("retry.random.bound.topic.", -1, Type.INT),
    RETRY_RANDOM_BOUND_APP_PREFIX("retry.random.bound.app.", -1, Type.INT),
    RETRY_RATE("retry.rate", -1, Type.INT),
    BROADCAST_INDEX_RESET_ENABLE("consume.broadcast.index.reset.enable", true, Type.BOOLEAN),
    BROADCAST_INDEX_RESET_INTERVAL("consume.broadcast.index.reset.interval", 1000 * 60 * 5, Type.INT),
    BROADCAST_INDEX_RESET_TIME("consume.broadcast.index.reset.time", 1000 * 60 * 60 * 24 * 2, Type.INT),

    ;
    private String name;
    private Object value;
    private PropertyDef.Type type;

    ConsumeConfigKey(String name, Object value, PropertyDef.Type type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public Type getType() {
        return type;
    }

}
