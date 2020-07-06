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
package org.joyqueue.broker.limit.config;

import org.joyqueue.toolkit.config.PropertyDef;

/**
 * LimitConfigKey
 *
 * author: gaohaoxiang
 * date: 2019/5/16
 */
public enum LimitConfigKey implements PropertyDef {

    // 是否启用
    ENABLE("limit.enable", true, PropertyDef.Type.BOOLEAN),

    // 限流后延时
//    DELAY("limit.delay", LimitConfig.DELAY_DYNAMIC, PropertyDef.Type.INT),
    DELAY("limit.delay", 1000, PropertyDef.Type.INT),

    // 最大延时
    MAX_DELAY("limit.delay.max", 1000, PropertyDef.Type.INT),
    // 最小延时
    MIN_DELAY("limit.delay.min", 100, PropertyDef.Type.INT),

    // 拒绝策略
    REJECTED_STRATEGY("limit.rejected.strategy", "delay", PropertyDef.Type.STRING),

    ;

    private String name;
    private Object value;
    private Type type;

    LimitConfigKey(String name, Object value, Type type) {
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