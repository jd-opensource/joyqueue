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
package org.joyqueue.broker.monitor.config;

import org.joyqueue.toolkit.config.PropertyDef;

/**
 * broker监控配置key
 *
 * author: gaohaoxiang
 * date: 2018/10/10
 */
public enum BrokerMonitorConfigKey implements PropertyDef {

    ENABLE("stat.enable", true, PropertyDef.Type.BOOLEAN),
    STAT_SAVE_FILE("stat.save.file", "/store/stat", PropertyDef.Type.STRING),
    STAT_SAVE_FILE_NEW("stat.save.file.new", "/monitor/stat", PropertyDef.Type.STRING),
    STAT_SAVE_INTERVAL("stat.save.interval", 1000 * 30, PropertyDef.Type.INT),

    ;

    private String name;
    private Object value;
    private PropertyDef.Type type;

    BrokerMonitorConfigKey(String name, Object value, PropertyDef.Type type) {
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