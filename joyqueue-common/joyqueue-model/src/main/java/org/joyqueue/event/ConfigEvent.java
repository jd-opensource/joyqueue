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
package org.joyqueue.event;

@Deprecated
public class ConfigEvent extends MetaEvent {
    private String group;
    private String key;
    private String value;

    public ConfigEvent() {
    }

    public ConfigEvent(EventType type, String group, String key, String value) {
        super(type);
        this.group = group;
        this.key = key;
        this.value = value;
    }
    @Override
    public String getTypeName() {
        return getClass().getTypeName();
    }
    public void setGroup(String group) {
        this.group = group;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getGroup() {
        return group;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public static ConfigEvent add(String group, String key, String value) {
        return new ConfigEvent(EventType.ADD_CONFIG, group, key, value);
    }

    public static ConfigEvent update(String group, String key, String value) {
        return new ConfigEvent(EventType.UPDATE_CONFIG, group, key, value);
    }

    public static ConfigEvent remove(String group, String key, String value) {
        return new ConfigEvent(EventType.REMOVE_CONFIG, group, key, value);
    }

    @Override
    public String toString() {
        return "ConfigEvent{" +
                ", group='" + group + '\'' +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
