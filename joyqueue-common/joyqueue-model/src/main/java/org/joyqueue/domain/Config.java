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
package org.joyqueue.domain;

import java.util.Objects;

/**
 * @author wylixiaobin
 * Date: 2018/9/4
 */
public class Config {
    protected String group;
    protected String key;
    protected String value;
    protected int priority;

    public String getId(){
        return new StringBuilder(30).append(group).append(".").append(key).toString();
    }

    public String getGroup() {
        return group;
    }

    public Config() {

    }

    public Config(String group, String key, String value) {
        this.group = group;
        this.key = key;
        this.value = value;
    }

    public Config(String group, String key, String value, int priority) {
        this.group = group;
        this.key = key;
        this.value = value;
        this.priority = priority;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Config)) return false;
        Config config = (Config) o;
        return Objects.equals(group, config.group) &&
                Objects.equals(key, config.key) &&
                Objects.equals(value, config.value);
    }

    @Override
    public int hashCode() {

        return Objects.hash(group, key, value);
    }

    @Override
    public String toString() {
        return "Config{" +
                "group='" + group + '\'' +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
