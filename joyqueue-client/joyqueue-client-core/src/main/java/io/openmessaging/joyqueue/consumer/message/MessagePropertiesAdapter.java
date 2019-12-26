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
package io.openmessaging.joyqueue.consumer.message;

import org.joyqueue.client.internal.consumer.domain.ConsumeMessage;
import io.openmessaging.KeyValue;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * MessagePropertiesAdapter
 *
 * author: gaohaoxiang
 * date: 2019/3/1
 */
// TODO 缺少默认值方法
public class MessagePropertiesAdapter implements KeyValue {

    private ConsumeMessage message;

    public MessagePropertiesAdapter(ConsumeMessage message) {
        this.message = message;
    }

    @Override
    public KeyValue put(String key, boolean value) {
        return this;
    }

    @Override
    public KeyValue put(String key, short value) {
        return this;
    }

    @Override
    public KeyValue put(String key, int value) {
        return this;
    }

    @Override
    public KeyValue put(String key, long value) {
        return this;
    }

    @Override
    public KeyValue put(String key, double value) {
        return this;
    }

    @Override
    public KeyValue put(String key, String value) {
        return this;
    }

    @Override
    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = message.getAttribute(key);
        return (value == null ? defaultValue : Boolean.valueOf(value));
    }

    @Override
    public short getShort(String key) {
        return getShort(key, (short) 0);
    }

    @Override
    public short getShort(String key, short defaultValue) {
        String value = message.getAttribute(key);
        return (value == null ? defaultValue : Short.valueOf(value));
    }

    @Override
    public int getInt(String key) {
        return getInt(key, 0);
    }

//    @Override
    public int getInt(String key, int defaultValue) {
        String value = message.getAttribute(key);
        return (value == null ? 0 : Integer.valueOf(value));
    }

    @Override
    public long getLong(String key) {
        return getLong(key, 0);
    }

    @Override
    public long getLong(String key, long defaultValue) {
        String value = message.getAttribute(key);
        return (value == null ? 0 : Long.valueOf(value));
    }

    @Override
    public double getDouble(String key) {
        return getDouble(key, 0);
    }

//    @Override
    public double getDouble(String key, double defaultValue) {
        String value = message.getAttribute(key);
        return (value == null ? 0 : Double.valueOf(value));
    }

    @Override
    public String getString(String key) {
        return getString(key, null);
    }

//    @Override
    public String getString(String key, String defaultValue) {
        String value = message.getAttribute(key);
        return (value == null ? null : value);
    }

    @Override
    public Set<String> keySet() {
        Map<String, String> attributes = message.getAttributes();
        if (attributes == null) {
            return Collections.emptySet();
        }
        return attributes.keySet();
    }

    @Override
    public boolean containsKey(String key) {
        return message.containsAttribute(key);
    }
}