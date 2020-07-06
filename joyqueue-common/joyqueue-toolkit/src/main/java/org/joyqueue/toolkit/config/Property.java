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

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 配置项
 * Created by yangyang115 on 18-7-23.
 */
public class Property {

    public static final String CPU_CORES = "host.cpucores";
    public static final String MEMORY = "host.memory";
    public static final String DISK_SIZE = "host.disksize";
    public static final String DATA_CENTER = "host.datacenter";
    public static final String SYSTEM_NAME = "system.name";
    public static final String APPLICATION_NAME = "application.name";
    public static final String APPLICATION_DEPLOY_NAME = "application.deploy.name";
    public static final String APPLICATION_ID = "application.id";
    public static final String APPLICATION_GROUP = "application.group";
    public static final String CLIENT_VERSION = "client.version";
    public static final String USER_HOME = "user.home";
    public static final String JAVA_IO_TEMPDIR = "java.io.tmpdir";
    public static final String OS_NAME = "os.name";
    public static final String OS_TYPE = "os.type";
    public static final String PID = "pid";
    public static final String JAVA_VERSION = "java.version";
    public static final String APPLICATION_DATA_PATH = "application.data.path";

    public static final String AUTO_LISTENER = "autoListener";

    //名称
    protected String name;
    //键
    protected String key;
    //值
    protected Object value;
    // 组
    protected String group;
    //版本
    protected long version;
    //优先级
    protected int priority;

    public Property() {
    }

    public Property(String key) {
        this.key = key;
    }

    public Property(String name, String key, Object value) {
        this(name, key, value, 0, 0);
    }

    public Property(String name, String key, Object value, long version, int priority) {
        this(name, key, value, null, version, priority);
    }

    public Property(String name, String key, Object value, String group, long version, int priority) {
        this.name = name;
        this.key = key;
        this.value = value;
        this.group = group;
        this.version = version;
        this.priority = priority;
    }

    public String getName() {
        return name;
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    public String getGroup() {
        return group;
    }

    public long getVersion() {
        return version;
    }

    public int getPriority() {
        return priority;
    }


    public String getString() {
        return Converts.getString(value);
    }

    /**
     * 获取字符串参数值
     *
     * @param def 默认值
     * @return 参数值
     */
    public String getString(final String def) {
        return Converts.getString(value, def);
    }

    /**
     * 获取日期参数值，日期是从EPOCH的毫秒数
     *
     * @param def 默认值
     * @return 参数值
     */
    public Date getDate(final Date def) {
        return Converts.getDate(value, def);
    }

    /**
     * 获取日期参数值，日期格式为字符串
     * <p>
     * ()
     *
     * @param format 日期格式
     * @return 参数值
     */
    public Date getDate(final SimpleDateFormat format) {
        return Converts.getDate(value, format, null);
    }

    /**
     * 获取日期参数值，日期格式为字符串
     * <p>
     * ()
     *
     * @param format 日期格式
     * @param def    默认值
     * @return 参数值
     */
    public Date getDate(final SimpleDateFormat format, final Date def) {
        return Converts.getDate(value, format, def);
    }

    /**
     * 获取单精度浮点数参数值
     *
     * @return 参数值
     */
    public Float getFloat() {
        return Converts.getFloat(value, null);
    }

    /**
     * 获取单精度浮点数参数值
     *
     * @param def 默认值
     * @return 参数值
     */
    public Float getFloat(final Float def) {
        return Converts.getFloat(value, def);
    }

    /**
     * 获取双精度浮点数参数值
     *
     * @return 参数值
     */
    public Double getDouble() {
        return Converts.getDouble(value, null);
    }

    /**
     * 获取双精度浮点数参数值
     *
     * @param def 默认值
     * @return 参数值
     */
    public Double getDouble(final Double def) {
        return Converts.getDouble(value, def);
    }

    /**
     * 获取长整形参数值
     *
     * @return 参数值
     */
    public Long getLong() {
        return Converts.getLong(value, null);
    }

    /**
     * 获取长整形参数值
     *
     * @param def 默认值
     * @return 参数值
     */
    public Long getLong(final Long def) {
        return Converts.getLong(value, def);
    }

    /**
     * 获取整形参数值
     *
     * @return 参数值
     */
    public Integer getInteger() {
        return Converts.getInteger(value, null);
    }

    /**
     * 获取整形参数值
     *
     * @param def 默认值
     * @return 参数值
     */
    public Integer getInteger(final Integer def) {
        return Converts.getInteger(value, def);
    }

    /**
     * 获取短整形参数值
     *
     * @return 参数值
     */
    public Short getShort() {
        return Converts.getShort(value, null);
    }

    /**
     * 获取短整形参数值
     *
     * @param def 默认值
     * @return 参数值
     */
    public Short getShort(final Short def) {
        return Converts.getShort(value, def);
    }

    /**
     * 获取字节参数值
     *
     * @return 参数值
     */
    public Byte getByte() {
        return Converts.getByte(value, null);
    }


    /**
     * 获取字节参数值
     *
     * @param def 默认值
     * @return 参数值
     */
    public Byte getByte(final Byte def) {
        return Converts.getByte(value, def);
    }

    /**
     * 获取布尔参数值
     *
     * @return 参数值
     */
    public Boolean getBoolean() {
        return Converts.getBoolean(value, null);
    }

    /**
     * 获取布尔参数值
     *
     * @param def 默认值
     * @return 参数值
     */
    public Boolean getBoolean(final Boolean def) {
        return Converts.getBoolean(value, def);
    }

    /**
     * 获取长整形自然数参数值
     *
     * @param def 默认值
     * @return 参数值
     */
    public Long getNatural(final Long def) {
        return Converts.getNatural(value, def);
    }

    /**
     * 获取整形自然数参数值
     *
     * @param def 默认值
     * @return 参数值
     */
    public Integer getNatural(final Integer def) {
        return Converts.getNatural(value, def);
    }

    /**
     * 获取短整形自然数参数值
     *
     * @param def 默认值
     * @return 参数值
     */
    public Short getNatural(final Short def) {
        return Converts.getNatural(value, def);
    }

    /**
     * 获取字节自然数参数值
     *
     * @param def 默认值
     * @return 参数值
     */
    public Byte getNatural(final Byte def) {
        return Converts.getNatural(value, def);
    }

    /**
     * 获取长整形正整数参数值
     *
     * @param def 默认值
     * @return 参数值
     */
    public Long getPositive(final Long def) {
        return Converts.getPositive(value, def);
    }

    /**
     * 获取整形正整数参数值
     *
     * @param def 默认值
     * @return 参数值
     */
    public Integer getPositive(final Integer def) {
        return Converts.getPositive(value, def);
    }

    /**
     * 获取短整形正整数参数值
     *
     * @param def 默认值
     * @return 参数值
     */
    public Short getPositive(final Short def) {
        return Converts.getPositive(value, def);
    }

    /**
     * 获取字节正整数参数值
     *
     * @param def 默认值
     * @return 参数值
     */
    public Byte getPositive(final Byte def) {
        return Converts.getPositive(value, def);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Property property = (Property) o;

        if (name != null ? !name.equals(property.name) : property.name != null) {
            return false;
        }
        if (key != null ? !key.equals(property.key) : property.key != null) {
            return false;
        }
        return value != null ? value.equals(property.value) : property.value == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (key != null ? key.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Property{" +
                "name='" + name + '\'' +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }


    public enum Type {
        BOOLEAN, STRING, INT, SHORT, LONG, DOUBLE, OBJECT
    }


}
