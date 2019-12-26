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
package org.joyqueue.toolkit.config;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 上下文
 */
public class Context implements Serializable, Cloneable {

    // 参数
    protected ConcurrentHashMap<String, Object> parameters = new ConcurrentHashMap<String, Object>();

    public Context() {
    }

    public Context(final Map<String, Object> parameters) {
        if (parameters != null) {
            this.parameters.putAll(parameters);
        }
    }

    /**
     * 获取指定类型的参数
     *
     * @param name  参数名称
     * @param clazz 类型
     * @return 参数对象
     */
    public <T> T getObject(final String name, final Class<T> clazz) {
        return (T) parameters.get(name);
    }

    /**
     * 获取对象参数
     *
     * @param name 参数名称
     * @return 参数对象
     */
    public Object getObject(final String name) {
        return parameters.get(name);
    }

    /**
     * 获取字符串参数
     *
     * @param name         名称
     * @param defaultValue 默认值
     * @return 字符串参数
     */
    public String getString(final String name, final String defaultValue) {
        Object result = parameters.get(name);
        if (result == null) {
            return defaultValue;
        }
        String value = result.toString();
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return value;
    }

    /**
     * 获取字节参数
     *
     * @param name         名称
     * @param defaultValue 默认值
     * @return 浮点数参数
     */
    public Byte getByte(final String name, final Byte defaultValue) {
        Object result = parameters.get(name);
        if (result == null) {
            return defaultValue;
        } else if (result instanceof Number) {
            return ((Number) result).byteValue();
        }
        String text = result.toString();
        if (text == null || text.isEmpty()) {
            return defaultValue;
        }
        try {
            return Byte.parseByte(text);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 获取短整数参数
     *
     * @param name         名称
     * @param defaultValue 默认值
     * @return 浮点数参数
     */
    public Short getShort(final String name, final Short defaultValue) {
        Object result = parameters.get(name);
        if (result == null) {
            return defaultValue;
        } else if (result instanceof Number) {
            return ((Number) result).shortValue();
        }
        String text = result.toString();
        if (text == null || text.isEmpty()) {
            return defaultValue;
        }
        try {
            return Short.parseShort(text);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 获取整数参数
     *
     * @param name         名称
     * @param defaultValue 默认值
     * @return 整数
     */
    public Integer getInteger(final String name, final Integer defaultValue) {
        Object result = parameters.get(name);
        if (result == null) {
            return defaultValue;
        } else if (result instanceof Number) {
            return ((Number) result).intValue();
        }
        String text = result.toString();
        if (text == null || text.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 获取长整形参数
     *
     * @param name         名称
     * @param defaultValue 默认值
     * @return 长整形参数
     */
    public Long getLong(final String name, final Long defaultValue) {
        Object result = parameters.get(name);
        if (result == null) {
            return defaultValue;
        } else if (result instanceof Number) {
            return ((Number) result).longValue();
        }
        String text = result.toString();
        if (text == null || text.isEmpty()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 获取单精度浮点数参数
     *
     * @param name         名称
     * @param defaultValue 默认值
     * @return 浮点数参数
     */
    public Float getFloat(final String name, final Float defaultValue) {
        Object result = parameters.get(name);
        if (result == null) {
            return defaultValue;
        } else if (result instanceof Number) {
            return ((Number) result).floatValue();
        }
        String text = result.toString();
        if (text == null || text.isEmpty()) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(text);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 获取双精度浮点数参数
     *
     * @param name         名称
     * @param defaultValue 默认值
     * @return 浮点数参数
     */
    public Double getDouble(final String name, final Double defaultValue) {
        Object result = parameters.get(name);
        if (result == null) {
            return defaultValue;
        } else if (result instanceof Number) {
            return ((Number) result).doubleValue();
        }
        String text = result.toString();
        if (text == null || text.isEmpty()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 获取布尔值
     *
     * @param name         名称
     * @param defaultValue 默认值
     * @return 布尔值
     */
    public Boolean getBoolean(final String name, final Boolean defaultValue) {
        Object result = parameters.get(name);
        if (result == null) {
            return defaultValue;
        } else if (result instanceof Number) {
            return ((Number) result).longValue() != 0;
        } else if (result instanceof Boolean) {
            return (Boolean) result;
        }
        String value = result.toString();
        if ("true".equalsIgnoreCase(value)) {
            return true;
        } else if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        try {
            return Long.parseLong(value) != 0;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 获取日期参数值，日期是从EPOCH的毫秒数
     *
     * @param key          参数名称
     * @param defaultValue 默认值
     * @return 参数值
     */
    public Date getDate(final String key, final Date defaultValue) {
        Object result = parameters.get(key);
        if (result == null) {
            return defaultValue;
        } else if (result instanceof Number) {
            return new Date(((Number) result).longValue());
        } else if (result instanceof Date) {
            return (Date) result;
        }
        String value = result.toString();
        try {
            return new Date((Long.parseLong(value)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 获取日期参数值，日期格式为字符串
     *
     * @param key          参数名称
     * @param format       日期格式
     * @param defaultValue 默认值
     * @return 参数值
     */
    public Date getDate(final String key, final SimpleDateFormat format, final Date defaultValue) {
        Object result = parameters.get(key);
        if (result == null) {
            return defaultValue;
        } else if (result instanceof Number) {
            return new Date(((Number) result).longValue());
        } else if (result instanceof Date) {
            return (Date) result;
        } else if (format == null) {
            return defaultValue;
        }
        String value = result.toString();
        try {
            return format.parse(value);
        } catch (ParseException e) {
            return defaultValue;
        }
    }

    /**
     * 获取正整数参数
     *
     * @param name         名称
     * @param defaultValue 默认值
     * @return 正整数
     */
    public Byte getPositive(final String name, final Byte defaultValue) {
        if (defaultValue != null && defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        Byte result = getByte(name, defaultValue);
        return result != null && result <= 0 ? defaultValue : result;
    }

    /**
     * 获取正整数参数
     *
     * @param name         名称
     * @param defaultValue 默认值
     * @return 正整数
     */
    public Short getPositive(final String name, final Short defaultValue) {
        if (defaultValue != null && defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        Short result = getShort(name, defaultValue);
        return result != null && result <= 0 ? defaultValue : result;
    }

    /**
     * 获取正整数参数
     *
     * @param name         名称
     * @param defaultValue 默认值
     * @return 正整数
     */
    public Integer getPositive(final String name, final Integer defaultValue) {
        if (defaultValue != null && defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        Integer result = getInteger(name, defaultValue);
        return result != null && result <= 0 ? defaultValue : result;
    }

    /**
     * 获取长正整数参数
     *
     * @param name         名称
     * @param defaultValue 默认值
     * @return 长正整数
     */
    public Long getPositive(final String name, final Long defaultValue) {
        if (defaultValue != null && defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        Long result = getLong(name, defaultValue);
        return result != null && result <= 0 ? defaultValue : result;
    }

    /**
     * 获取短整数自然数参数
     *
     * @param name         名称
     * @param defaultValue 默认值
     * @return 自然数
     */
    public Short getNatural(final String name, final Short defaultValue) {
        if (defaultValue != null && defaultValue < 0) {
            throw new IllegalArgumentException("defaultValue < 0");
        }
        Short result = getShort(name, defaultValue);
        return result != null && result < 0 ? defaultValue : result;
    }

    /**
     * 获取短整数自然数参数
     *
     * @param name         名称
     * @param defaultValue 默认值
     * @return 自然数
     */
    public Byte getNatural(final String name, final Byte defaultValue) {
        if (defaultValue != null && defaultValue < 0) {
            throw new IllegalArgumentException("defaultValue < 0");
        }
        Byte result = getByte(name, defaultValue);
        return result != null && result < 0 ? defaultValue : result;
    }

    /**
     * 获取整数自然数参数
     *
     * @param name         名称
     * @param defaultValue 默认值
     * @return 自然数
     */
    public Integer getNatural(final String name, final Integer defaultValue) {
        if (defaultValue != null && defaultValue < 0) {
            throw new IllegalArgumentException("defaultValue < 0");
        }
        Integer result = getInteger(name, defaultValue);
        return result != null && result < 0 ? defaultValue : result;
    }

    /**
     * 获取长整数自然数参数
     *
     * @param name         名称
     * @param defaultValue 默认值
     * @return 自然数
     */
    public Long getNatural(final String name, final Long defaultValue) {
        if (defaultValue != null && defaultValue < 0) {
            throw new IllegalArgumentException("defaultValue < 0");
        }
        Long result = getLong(name, defaultValue);
        return result != null && result < 0 ? defaultValue : result;
    }

    /**
     * 存放键值对
     *
     * @param key   键
     * @param value 值
     * @return 先前的对象
     */
    public Object put(final String key, final Object value) {
        return parameters.put(key, value);
    }

    /**
     * 存放键值对
     *
     * @param key   键
     * @param value 值
     * @return 先前的对象
     */
    public Object putIfAbsent(final String key, final Object value) {
        return parameters.putIfAbsent(key, value);
    }

    /**
     * 存放键值
     *
     * @param map 键值对
     */
    public void put(final Map<String, ?> map) {
        if (map != null) {
            parameters.putAll(map);
        }
    }

    /**
     * 存放键值
     *
     * @param context 上下文
     */
    public void put(final Context context) {
        if (context != null) {
            parameters.putAll(context.parameters);
        }
    }

    /**
     * 删除参数
     *
     * @param key 参数名称
     * @return 参数值
     */
    public Object remove(final String key) {
        return parameters.remove(key);
    }

    /**
     * 清理所有参数
     */
    public void remove() {
        parameters.clear();
    }

    /**
     * 转换成Map对象
     *
     * @return Map对象
     */
    public Map<String, Object> toMap() {
        return new HashMap<String, Object>(parameters);
    }

    /**
     * 获取迭代器
     *
     * @return 迭代器
     */
    public Iterator<Map.Entry<String, Object>> iterator() {
        return parameters.entrySet().iterator();
    }

    @Override
    public Context clone() {
        Context result = new Context(parameters);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Context context = (Context) o;

        return parameters != null ? parameters.equals(context.parameters) : context.parameters == null;

    }

    @Override
    public int hashCode() {
        return parameters != null ? parameters.hashCode() : 0;
    }
}
