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
package org.joyqueue.broker.config;

import org.joyqueue.toolkit.config.Property;
import org.joyqueue.toolkit.config.PropertySupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 配置集
 * Created by yangyang115 on 18-7-23.
 */
public class Configuration implements PropertySupplier {
    protected static final Logger logger = LoggerFactory.getLogger(Configuration.class);
    protected static final int DEFAULT_CONFIGURATION_PRIORITY = 10;
    protected static final long DEFAULT_CONFIGURATION_VERSION = 1;
    protected static final String DEFAULT_CONFIGURATION_NAME = "_BROKER_CONFIG_";

    //名称
    protected String name = DEFAULT_CONFIGURATION_NAME;
    //数据版本
    protected long version = DEFAULT_CONFIGURATION_VERSION;
    //优先级
    protected int priority = DEFAULT_CONFIGURATION_PRIORITY;
    protected Map<String, Property> properties =new HashMap<>();



    public Configuration() {
    }

    public Configuration(String name, Collection<Property> properties, long version, int priority) {
        this.name = name;
        this.version = version;
        this.priority = priority;
        addProperties(properties);
    }

    public Configuration(String name, Map<?, ?> properties, long version, int priority) {
        this.name = name;
        this.version = version;
        this.priority = priority;
        if (properties != null) {
            List<Property> items = new ArrayList<Property>(properties.size());
            for (Map.Entry<?, ?> entry : properties.entrySet()) {
                items.add(new Property(name, entry.getKey().toString(), entry.getValue().toString(), version, priority));
            }
            addProperties(items);
        }
    }

    public Configuration(String name, long version, int priority) {
        this.name = name;
        this.version = version;
        this.priority = priority;
    }

    public Configuration(String name, List<Configuration> configurations, long version) {
        this.name = name;
        this.version = version;
        List<Property> items = new LinkedList<Property>();
        if (configurations != null) {
            for (Configuration configuration : configurations) {
                if (configuration != null && configuration.size() > 0) {
                    for (Property property : configuration.properties.values()) {
                        items.add(property);
                    }

                    //获取配置的最大优先级和该优先级的版本
                    if (this.priority < configuration.priority) {
                        this.priority = configuration.priority;
                    }
                }
            }
        }
        addProperties(items);
    }

    /**
     * 添加属性集，处理数组配置方式
     *
     * @param collection
     */
    protected void addProperties(final Collection<Property> collection) {
        if (collection == null || collection.isEmpty()) {
            return;
        }
        //处理数组的配置方式
        //laf.config.manager.parameters[autoListener] = true
        //laf.config.resources[0].name=ucc_test
        Map<String, Object> context = new HashMap<String, Object>();
        for (Property item : collection) {
            if (item.getKey() == null || item.getValue() == null) {
                return;
            }
            properties.put(item.getKey(), item);
            process(item.getKey(), 0, item.getValue(), context);
        }
        if (!context.isEmpty()) {
            processArray(context, false);
            Property property;
            for (Map.Entry<String, Object> entry : context.entrySet()) {
                if (!properties.containsKey(entry.getKey())) {
                    //动态创建的属性设置成不存储
                    property = new Property(name, entry.getKey(), entry.getValue(), version, priority);
                    properties.put(entry.getKey(), property);
                }
            }
        }
    }

    /**
     * 添加属性
     *
     * @param key
     * @param value
     */
    public Property addProperty(final String key, final String value, final String group) {
        Property property = new Property(name, key, value, group, 0, 0);
        properties.put(key, property);
        return property;
    }

    public Property addProperty(final String key, final String value, final String group,int priority) {
        Property property = new Property(name, key, value, group, 0, priority);
        properties.put(key, property);
        return property;
    }

    public Property addProperty(final String key, final String value) {
        return addProperty(key, value, null);
    }

    protected static void process(final String source, final int start, final Object value, final Map<String, Object> context) {
        //找到"[xxx]"，"]"为结尾或后面跟着".xxx"
        int from = start;
        while (true) {
            int left = source.indexOf('[', from);
            if (left > 0) {
                int right = source.indexOf(']', left + 1);
                if (right > 0) {
                    String key = source.substring(start, left);
                    String index = source.substring(left + 1, right);
                    if (index != null && !index.isEmpty()
                            && (right == source.length() - 1
                            || source.charAt(right + 1) == '.' && right < source.length() - 2)) {
                        Object v = context.get(key);
                        if (v == null || !(v instanceof Map)) {
                            v = new HashMap<String, Object>();
                            context.put(key, v);
                        }
                        if (right == source.length() - 1) {
                            //']'在最后
                            ((Map<String, Object>) v).put(index, value);
                        } else {
                            //"]."
                            Object child = ((Map) v).get(index);
                            if (child == null || !(child instanceof Map)) {
                                child = new HashMap<String, Object>();
                                ((Map) v).put(index, child);
                            }
                            process(source, right + 2, value, (Map<String, Object>) child);
                        }
                        return;
                    } else {
                        //查找下一个"[xxx]"
                        from = right + 1;
                    }
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        //剩余字符串
        if (start > 0 && start < source.length()) {
            //后面作为整个字符串出来
            context.put(source.substring(start), value);
        }
    }

    protected static Object[] processArray(final Map<String, Object> context, final boolean flag) {
        if (context.isEmpty()) {
            return null;
        }
        //判断值是否可以转换成数组
        int v;
        int max = 0;
        boolean isArray = true;
        Object[] array;
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            if (flag && isArray) {
                try {
                    v = Integer.parseInt(entry.getKey());
                    if (v >= 0) {
                        if (max < v) {
                            max = v;
                        }
                    } else {
                        isArray = false;
                    }
                } catch (NumberFormatException e) {
                    isArray = false;
                }
            }
            if (entry.getValue() instanceof Map) {
                array = processArray((Map<String, Object>) entry.getValue(), true);
                if (array != null) {
                    entry.setValue(array);
                }
            }
        }
        if (flag && isArray) {
            Object[] result = new Object[max + 1];
            for (Map.Entry<String, Object> entry : context.entrySet()) {
                result[Integer.parseInt(entry.getKey())] = entry.getValue();
            }
            return result;
        } else {
            return null;
        }
    }

    @Override
    public Property getProperty(final String key) {
        return properties.get(key);
    }

    @Override
    public Property getOrCreateProperty(final String key) {
        Property property = getProperty(key);
        if (property == null) {
            //when key not exists ,the created property should assign low version and low priority
            return new Property(this.name, key, null, 0, 0);
        }
        return property;
    }

    /**
     * 获取配置集下所有属性
     *
     * @return
     * @see Configuration#getProperties()
     */
    @Deprecated
    public Collection<Property> get() {
        return getProperties();
    }

    @Override
    public List<Property> getProperties() {
        return properties == null ? new ArrayList<Property>(0) : new ArrayList<Property>(properties.values());
    }

    @Override
    public List<Property> getPrefix(final String prefix) {
        List<Property> result = new ArrayList<Property>();
        if (prefix == null || prefix.isEmpty() || properties == null) {
            return result;
        }
        for (Map.Entry<String, Property> entry : properties.entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                result.add(entry.getValue());
            }
        }
        return result;
    }

    /**
     * 验证是否存在 key
     *
     * @param key
     * @return
     */
    public boolean contains(final String key) {
        return properties.containsKey(key);
    }

    public String getName() {
        return name;
    }

    public long getVersion() {
        return version;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isEmpty() {
        return properties.isEmpty();
    }

    public int size() {
        return properties == null ? 0 : properties.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Configuration that = (Configuration) o;

        return properties.equals(that.properties);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "properties=" + properties +
                ", name='" + name + '\'' +
                ", version=" + version +
                '}';
    }
}
