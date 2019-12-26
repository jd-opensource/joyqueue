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
package io.openmessaging.joyqueue.producer.interceptor;

import com.google.common.collect.Sets;
import org.joyqueue.client.internal.producer.interceptor.ProduceContext;
import io.openmessaging.KeyValue;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * ContextAttributeAdapter
 *
 * author: gaohaoxiang
 * date: 2019/3/1
 */
// TODO 缺少默认值方法
public class ContextAttributeAdapter implements KeyValue {

    private ProduceContext context;

    public ContextAttributeAdapter(ProduceContext context) {
        this.context = context;
    }

    @Override
    public KeyValue put(String key, boolean value) {
        context.putAttribute(key, value);
        return this;
    }

    @Override
    public KeyValue put(String key, short value) {
        context.putAttribute(key, value);
        return this;
    }

    @Override
    public KeyValue put(String key, int value) {
        context.putAttribute(key, value);
        return this;
    }

    @Override
    public KeyValue put(String key, long value) {
        context.putAttribute(key, value);
        return this;
    }

    @Override
    public KeyValue put(String key, double value) {
        context.putAttribute(key, value);
        return this;
    }

    @Override
    public KeyValue put(String key, String value) {
        context.putAttribute(key, value);
        return this;
    }

    @Override
    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        Object value = context.getAttribute(key);
        return (value == null ? defaultValue : Boolean.valueOf(String.valueOf(value)));
    }

    @Override
    public short getShort(String key) {
        return getShort(key, (short) 0);
    }

    @Override
    public short getShort(String key, short defaultValue) {
        Object value = context.getAttribute(key);
        return (value == null ? defaultValue : Short.valueOf(String.valueOf(value)));
    }

    @Override
    public int getInt(String key) {
        return getInt(key, 0);
    }

//    @Override
    public int getInt(String key, int defaultValue) {
        Object value = context.getAttribute(key);
        return (value == null ? 0 : Integer.valueOf(String.valueOf(value)));
    }

    @Override
    public long getLong(String key) {
        return getLong(key, 0);
    }

    @Override
    public long getLong(String key, long defaultValue) {
        Object value = context.getAttribute(key);
        return (value == null ? 0 : Long.valueOf(String.valueOf(value)));
    }

    @Override
    public double getDouble(String key) {
        return getDouble(key, 0);
    }

//    @Override
    public double getDouble(String key, double defaultValue) {
        Object value = context.getAttribute(key);
        return (value == null ? defaultValue : Double.valueOf(String.valueOf(value)));
    }

    @Override
    public String getString(String key) {
        return getString(key, null);
    }

//    @Override
    public String getString(String key, String defaultValue) {
        Object value = context.getAttribute(key);
        return (value == null ? null : String.valueOf(value));
    }

    @Override
    public Set<String> keySet() {
        Map<Object, Object> attributes = context.getAttributes();
        if (attributes == null) {
            return Collections.emptySet();
        }
        Set<String> result = Sets.newHashSet();
        for (Map.Entry<Object, Object> entry : attributes.entrySet()) {
            result.add(String.valueOf(entry.getKey()));
        }
        return result;
    }

    @Override
    public boolean containsKey(String key) {
        return context.containsAttribute(key);
    }
}