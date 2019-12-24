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

import com.jd.laf.extension.Converts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 属性提供者
 */
public interface PropertySupplier {
    /**
     * 获取属性，没有则创建一个
     *
     * @param key 属性名
     * @return 属性
     */
    Property getOrCreateProperty(String key);

    /**
     * 获取属性
     *
     * @param key 属性名
     * @return 属性
     */
    Property getProperty(String key);

    /**
     * 获取所有属性
     *
     * @return
     */
    List<Property> getProperties();

    /**
     * 根据前缀查找属性
     *
     * @param prefix 前缀
     * @return
     */
    List<Property> getPrefix(String prefix);

    /**
     * Map属性提供者
     */
    class MapSupplier implements PropertySupplier {
        Map<String, Object> map;

        public MapSupplier(Map<String, Object> map) {
            this.map = map;
        }

        @Override
        public Property getOrCreateProperty(final String key) {
            if (key == null) {
                return null;
            }
            Property result = getProperty(key);
            return result == null ? new Property(null, key, null) : result;

        }

        @Override
        public Property getProperty(final String key) {
            if (key == null) {
                return null;
            }
            Object value = map == null ? null : map.get(key);
            return value == null ? null : new Property(null, key, value.toString());
        }

        @Override
        public List<Property> getProperties() {
            List<Property> result = new ArrayList<Property>(map == null ? 0 : map.size());
            if (map == null) {
                return result;
            }
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                result.add(new Property(null, entry.getKey(), entry.getValue()));
            }
            return result;
        }

        @Override
        public List<Property> getPrefix(final String prefix) {
            List<Property> result = new ArrayList<Property>();
            if (prefix == null || prefix.isEmpty() || map == null) {
                return result;
            }
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (entry.getKey().startsWith(prefix)) {
                    result.add(new Property(null, entry.getKey(), entry.getValue()));
                }
            }
            return result;
        }
    }

    default <T> T getValue(PropertyDef key) {
        return getValue(this, key);
    }

    static <T> T getValue(PropertySupplier supplier, PropertyDef key) {
        if (key == null) {
            throw new IllegalArgumentException("key can not be null");
        }
        return getValue(supplier, key.getName(), key.getType(), key.getValue());
    }

    static <T> T getValue(PropertySupplier supplier, String key, PropertyDef.Type type, Object defaultValue) {
        if (key == null) {
            throw new IllegalArgumentException("key can not be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("type can not be null");
        }
        Property property = supplier.getProperty(key);
        Object value = (property != null ? property.getValue() : defaultValue);
        switch (type) {
            case BOOLEAN:
                return (T) Converts.getBoolean(value, Converts.getBoolean(defaultValue));
            case STRING:
                return (T) Converts.getString(value, Converts.getString(defaultValue));
            case INT:
                return (T) Converts.getInteger(value, Converts.getInteger(defaultValue));
            case SHORT:
                return (T) Converts.getShort(value, Converts.getShort(defaultValue));
            case LONG:
                return (T) Converts.getLong(value, Converts.getLong(defaultValue));
            case DOUBLE:
                return (T) Converts.getDouble(value, Converts.getDouble(defaultValue));
            default:
                return (T) (value == null ? defaultValue : value);
        }
    }


    static <T> T getValue(PropertySupplier supplier, PropertyDef key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("key can not be null");
        }
        Object propertyValue = null;
        if(null != supplier) {
            Property property = supplier.getProperty(key.getName());
            if (property != null) {
                propertyValue = property.getValue();
            }
        }

        Object defaultValue = key.getValue();
        switch (key.getType()) {
            case BOOLEAN:
                return (T) Converts.getBoolean(propertyValue, Converts.getBoolean(value,Converts.getBoolean(defaultValue)));
            case STRING:
                return (T) Converts.getString(propertyValue, Converts.getString(value,Converts.getString(defaultValue)));
            case INT:
                return (T) Converts.getInteger(propertyValue, Converts.getInteger(value,Converts.getInteger(defaultValue)));
            case SHORT:
                return (T) Converts.getShort(propertyValue, Converts.getShort(value,Converts.getShort(defaultValue)));
            case LONG:
                return (T) Converts.getLong(propertyValue, Converts.getLong(value,Converts.getLong(defaultValue)));
            case DOUBLE:
                return (T) Converts.getDouble(propertyValue, Converts.getDouble(value,Converts.getDouble(defaultValue)));
            default:
                return (T) (propertyValue == null ? (value == null ? defaultValue : value) : propertyValue);
        }
    }

}
