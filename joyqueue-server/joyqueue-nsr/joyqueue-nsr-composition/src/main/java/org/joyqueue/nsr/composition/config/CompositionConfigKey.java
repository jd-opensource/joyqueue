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
package org.joyqueue.nsr.composition.config;

import org.joyqueue.toolkit.config.PropertyDef;

/**
 * CompositionConfigKey
 * author: gaohaoxiang
 * date: 2019/8/12
 */
public enum CompositionConfigKey implements PropertyDef {

    // 数据源
    SOURCE("nameserver.composition.source", "ignite", PropertyDef.Type.STRING),

    // 目标数据源
    TARGET("nameserver.composition.target", "sql", PropertyDef.Type.STRING),

    // 读数据源
    READ_SOURCE("nameserver.composition.read.source", "ignite", PropertyDef.Type.STRING),

    // 写数据源
    WRITE_SOURCE("nameserver.composition.write.source", "all", PropertyDef.Type.STRING),

    ;

    private String name;
    private Object value;
    private Type type;

    CompositionConfigKey(String name, Object value, Type type) {
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
