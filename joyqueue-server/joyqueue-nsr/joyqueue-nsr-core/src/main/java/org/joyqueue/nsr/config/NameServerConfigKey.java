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
package org.joyqueue.nsr.config;

import org.joyqueue.toolkit.config.PropertyDef;

/**
 * @author lixiaobin6
 * ${time} ${date}
 */
public enum NameServerConfigKey implements PropertyDef {
    NAMESERVICE_NAME("nameserver.nsr.name", "local", Type.STRING),
    NAMESERVER_ADDRESS("nameserver.nsr.address", "127.0.0.1:50092", Type.STRING),
    NAMESERVER_CACHE_ENABLE("nameserver.nsr.cache.enable", true, Type.BOOLEAN),
    NAMESERVER_CACHE_EXPIRE_TIME("nameserver.nsr.cache.expire.time", 1000 * 60, Type.INT),
    NAMESERVER_TOPIC_CACHE_EXPIRE_TIME("nameserver.nsr.topic.cache.expire.time", 1000 * 10, Type.INT),

    ;

    public static final String NAME_SERVER_CONFIG_PREFIX = "nameserver.";

    private String name;
    private Object value;
    private PropertyDef.Type type;

    NameServerConfigKey(String name, Object value, PropertyDef.Type type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    @Override
    public java.lang.String getName() {
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
