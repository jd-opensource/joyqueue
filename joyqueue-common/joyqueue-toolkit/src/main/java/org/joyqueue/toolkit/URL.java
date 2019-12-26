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
package org.joyqueue.toolkit;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author hexiaofeng
 */
public final class URL implements Serializable {

    public static final String FILE = "file";
    private static final long serialVersionUID = -1985165475234910535L;
    // 协议
    private final String protocol;
    // 名称
    private final String username;
    // 密码
    private final String password;
    // 主机
    private final String host;
    // 端口
    private final int port;
    // 路径
    private final String path;
    // 参数
    private final Map<String, String> parameters;

    protected URL() {
        this.protocol = null;
        this.username = null;
        this.password = null;
        this.host = null;
        this.port = 0;
        this.path = null;
        this.parameters = null;
    }

    public URL(String protocol, String host, int port) {
        this(protocol, null, null, host, port, null, null);
    }

    public URL(String protocol, String host, int port, Map<String, String> parameters) {
        this(protocol, null, null, host, port, null, parameters);
    }

    public URL(String protocol, String host, int port, String path) {
        this(protocol, null, null, host, port, path, null);
    }

    public URL(String protocol, String host, int port, String path, Map<String, String> parameters) {
        this(protocol, null, null, host, port, path, parameters);
    }

    public URL(String protocol, String username, String password, String host, int port, String path) {
        this(protocol, username, password, host, port, path, null);
    }

    public URL(String protocol, String username, String password, String host, int port, String path,
            Map<String, String> parameters) {
        if ((username == null || username.isEmpty()) && password != null && password.length() > 0) {
            throw new IllegalArgumentException("Invalid url, password without username!");
        }
        this.protocol = protocol;
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = (port < 0 ? 0 : port);
        this.path = path;
        // trim the beginning "/"
        while (path != null && path.startsWith("/")) {
            path = path.substring(1);
        }
        if (parameters == null) {
            parameters = new HashMap<String, String>();
        } else {
            parameters = new HashMap<String, String>(parameters);
        }
        this.parameters = Collections.unmodifiableMap(parameters);
    }

    /**
     * 用";"分割多个URL串
     *
     * @param url 链接
     * @return
     */
    public static String[] split(String url) {
        return split(url, ';');
    }

    /**
     * 分割多个URL串，保持括号
     *
     * @param url       链接
     * @param delimeter 分割符号
     * @return URL数组
     */
    public static String[] split(String url, char delimeter) {
        if (url == null || url.isEmpty()) {
            return new String[0];
        }
        List<String> urls = new ArrayList<String>();
        int braces = 0;
        String value = null;
        char[] chars = url.toCharArray();
        StringBuilder builder = new StringBuilder(url.length());
        for (char ch : chars) {
            if (ch == delimeter && braces == 0) {
                if (builder.length() > 0) {
                    value = builder.toString().trim();
                    if (!value.isEmpty()) {
                        urls.add(value);
                    }
                    builder.delete(0, builder.length());
                }
            } else {
                builder.append(ch);
                if (ch == '(') {
                    braces++;
                } else if (ch == ')') {
                    if (braces > 0) {
                        braces--;
                    }
                }
            }
        }
        if (builder.length() > 0) {
            value = builder.toString().trim();
            if (!value.isEmpty()) {
                urls.add(value);
            }
        }
        return urls.toArray(new String[urls.size()]);

    }

    /**
     * 把字符串转化成URL对象
     *
     * @param url 字符串
     * @return 新创建的URL对象
     */
    public static URL valueOf(String url) {
        if (url == null) {
            return null;
        }
        url = url.trim();
        if (url.isEmpty()) {
            return null;
        }
        String protocol = null;
        String username = null;
        String password = null;
        String host = null;
        int port = 0;
        String path = null;
        Map<String, String> parameters = null;

        // cloud://user:password@jss.360buy.com/mq?timeout=60000
        // file:/path/to/file.txt
        // zookeeper://10.10.10.10:2181,10.10.10.11:2181/?retryTimes=3
        // failover://(zookeeper://10.10.10.10:2181,10.10.10.11:2181;zookeeper://20.10.10.10:2181,20.10.10.11:2181)
        // ?interval=1000
        int j = 0;
        int i = url.indexOf(')');
        if (i >= 0) {
            i = url.indexOf('?', i);
        } else {
            i = url.indexOf("?");
        }
        if (i >= 0) {
            if (i < url.length() - 1) {
                String[] parts = url.substring(i + 1).split("\\&");
                parameters = new HashMap<String, String>();
                for (String part : parts) {
                    part = part.trim();
                    if (part.length() > 0) {
                        j = part.indexOf('=');
                        if (j > 0) {
                            if (j == part.length() - 1) {
                                parameters.put(part.substring(0, j), "");
                            } else {
                                parameters.put(part.substring(0, j), part.substring(j + 1));
                            }
                        } else if (j == -1) {
                            parameters.put(part, part);
                        }
                    }
                }
            }
            url = url.substring(0, i);
        }
        i = url.indexOf("://");
        if (i > 0) {
            protocol = url.substring(0, i);
            url = url.substring(i + 3);
        } else if (i < 0) {
            // case: file:/path/to/file.txt
            i = url.indexOf(":/");
            if (i > 0) {
                protocol = url.substring(0, i);
                // 保留路径符号“/”
                url = url.substring(i + 1);
            }
        }
        if (protocol == null || protocol.isEmpty()) {
            throw new IllegalStateException("url missing protocol: " + url);
        }
        if (protocol.equals(FILE)) {
            path = url;
            url = "";
        } else {
            i = url.lastIndexOf(')');
            if (i >= 0) {
                i = url.indexOf('/', i);
            } else {
                i = url.indexOf("/");
            }
            if (i >= 0) {
                path = url.substring(i + 1);
                url = url.substring(0, i);
            }
        }
        i = url.indexOf('(');
        if (i >= 0) {
            j = url.lastIndexOf(')');
            if (j >= 0) {
                url = url.substring(i + 1, j);
            } else {
                url = url.substring(i + 1);
            }
        } else {
            i = url.indexOf("@");
            if (i >= 0) {
                username = url.substring(0, i);
                j = username.indexOf(":");
                if (j >= 0) {
                    password = username.substring(j + 1);
                    username = username.substring(0, j);
                }
                url = url.substring(i + 1);
            }
            String[] values = url.split(":");
            if (values.length == 2) {
                // 排除zookeeper://192.168.1.2:2181,192.168.1.3:2181
                port = Integer.parseInt(values[1]);
                url = values[0];
            }
        }
        if (!url.isEmpty()) {
            host = url;
        }
        return new URL(protocol, username, password, host, port, path, parameters);
    }

    /**
     * URL编码
     *
     * @param value 字符串
     * @return 编码后的字符串
     */
    public static String encode(final String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException ignored) {
            return null;
        }
    }

    /**
     * URL解码
     *
     * @param value 编码后的字符串
     * @return 解码字符串
     */
    public static String decode(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException ignored) {
            return null;
        }
    }

    public String getProtocol() {
        return protocol;
    }

    public URL setProtocol(String protocol) {
        return new URL(protocol, username, password, host, port, path, getParameters());
    }

    public String getUsername() {
        return username;
    }

    public URL setUsername(String username) {
        return new URL(protocol, username, password, host, port, path, getParameters());
    }

    public String getPassword() {
        return password;
    }

    public URL setPassword(String password) {
        return new URL(protocol, username, password, host, port, path, getParameters());
    }

    public String getHost() {
        return host;
    }

    public URL setHost(String host) {
        return new URL(protocol, username, password, host, port, path, getParameters());
    }

    public int getPort() {
        return port;
    }

    public URL setPort(int port) {
        return new URL(protocol, username, password, host, port, path, getParameters());
    }

    public String getAddress() {
        return port <= 0 ? host : host + ":" + port;
    }

    public URL setAddress(String address) {
        int i = address.lastIndexOf(':');
        String host;
        int port = this.port;
        if (i >= 0) {
            host = address.substring(0, i);
            port = Integer.parseInt(address.substring(i + 1));
        } else {
            host = address;
        }
        return new URL(protocol, username, password, host, port, path, getParameters());
    }

    public String getPath() {
        return path;
    }

    public URL setPath(String path) {
        return new URL(protocol, username, password, host, port, path, getParameters());
    }

    public String getAbsolutePath() {
        if (path != null && !path.startsWith("/")) {
            return "/" + path;
        }
        return path;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    /**
     * 获取字符串参数值
     *
     * @param key    参数名称
     * @param decode 解码
     * @return 参数值
     */
    public String getString(final String key, final boolean decode) {
        return getString(key, null, decode);
    }

    /**
     * 获取字符串参数值
     *
     * @param key          参数名称
     * @param defaultValue 默认值
     * @param decode       解码
     * @return 参数值
     */
    public String getString(final String key, final String defaultValue, final boolean decode) {
        String value = getString(key, defaultValue);
        if (decode) {
            return decode(value);
        }
        return value;
    }

    /**
     * 获取字符串参数值
     *
     * @param key 参数名称
     * @return 参数值
     */
    public String getString(final String key) {
        return parameters.get(key);
    }

    /**
     * 获取字符串参数值
     *
     * @param key          参数名称
     * @param defaultValue 默认值
     * @return 参数值
     */
    public String getString(final String key, final String defaultValue) {
        String value = getString(key);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return value;
    }

    /**
     * 获取日期参数值，日期是从EPOCH的毫秒数
     *
     * @param key          参数名称
     * @param defaultValue 默认值
     * @return 参数值
     */
    public Date getDate(final String key, final Date defaultValue) {
        Long value = getLong(key, null);
        if (value == null) {
            return defaultValue;
        }
        return new Date(value);
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
        String value = getString(key);
        if (value == null || value.isEmpty() || format == null) {
            return defaultValue;
        }
        try {
            return format.parse(key);
        } catch (ParseException e) {
            return defaultValue;
        }
    }

    /**
     * 获取单精度浮点数参数值
     *
     * @param key          参数名称
     * @param defaultValue 默认值
     * @return 参数值
     */
    public Float getDouble(final String key, final Float defaultValue) {
        String value = getString(key);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 获取双精度浮点数参数值
     *
     * @param key          参数名称
     * @param defaultValue 默认值
     * @return 参数值
     */
    public Double getDouble(final String key, final Double defaultValue) {
        String value = getString(key);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 获取长整形参数值
     *
     * @param key          参数名称
     * @param defaultValue 默认值
     * @return 参数值
     */
    public Long getLong(final String key, final Long defaultValue) {
        String value = getString(key);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 获取整形参数值
     *
     * @param key          参数名称
     * @param defaultValue 默认值
     * @return 参数值
     */
    public Integer getInteger(final String key, final Integer defaultValue) {
        String value = getString(key);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 获取短整形参数值
     *
     * @param key          参数名称
     * @param defaultValue 默认值
     * @return 参数值
     */
    public Short getShort(final String key, final Short defaultValue) {
        String value = getString(key);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Short.parseShort(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 获取字节参数值
     *
     * @param key          参数名称
     * @param defaultValue 默认值
     * @return 参数值
     */
    public Byte getByte(final String key, final Byte defaultValue) {
        String value = getString(key);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Byte.parseByte(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 获取不二参数值
     *
     * @param key          参数名称
     * @param defaultValue 默认值
     * @return 参数值
     */
    public Boolean getBoolean(final String key, final Boolean defaultValue) {
        String value = getString(key);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    /**
     * 获取长整形自然数参数值
     *
     * @param key          参数名称
     * @param defaultValue 默认值
     * @return 参数值
     */
    public Long getNatrual(final String key, final Long defaultValue) {
        if (defaultValue != null && defaultValue < 0) {
            throw new IllegalArgumentException("defaultValue < 0");
        }
        Long value = getLong(key, defaultValue);
        if (value != null && value < 0) {
            return defaultValue;
        }
        return value;
    }

    /**
     * 获取整形自然数参数值
     *
     * @param key          参数名称
     * @param defaultValue 默认值
     * @return 参数值
     */
    public Integer getNatrual(final String key, final Integer defaultValue) {
        if (defaultValue != null && defaultValue < 0) {
            throw new IllegalArgumentException("defaultValue < 0");
        }
        Integer value = getInteger(key, defaultValue);
        if (value != null && value < 0) {
            return defaultValue;
        }
        return value;
    }

    /**
     * 获取短整形自然数参数值
     *
     * @param key          参数名称
     * @param defaultValue 默认值
     * @return 参数值
     */
    public Short getNatrual(final String key, final Short defaultValue) {
        if (defaultValue != null && defaultValue < 0) {
            throw new IllegalArgumentException("defaultValue < 0");
        }
        Short value = getShort(key, defaultValue);
        if (value != null && value < 0) {
            return defaultValue;
        }
        return value;
    }

    /**
     * 获取字节自然数参数值
     *
     * @param key          参数名称
     * @param defaultValue 默认值
     * @return 参数值
     */
    public Byte getNatrual(final String key, final Byte defaultValue) {
        if (defaultValue != null && defaultValue < 0) {
            throw new IllegalArgumentException("defaultValue < 0");
        }
        Byte value = getByte(key, defaultValue);
        if (value != null && value < 0) {
            return defaultValue;
        }
        return value;
    }

    /**
     * 获取长整形正整数参数值
     *
     * @param key          参数名称
     * @param defaultValue 默认值
     * @return 参数值
     */
    public Long getPositive(final String key, final Long defaultValue) {
        if (defaultValue != null && defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        Long value = getLong(key, defaultValue);
        if (value != null && value <= 0) {
            return defaultValue;
        }
        return value;
    }

    /**
     * 获取整形正整数参数值
     *
     * @param key          参数名称
     * @param defaultValue 默认值
     * @return 参数值
     */
    public Integer getPositive(final String key, final Integer defaultValue) {
        if (defaultValue != null && defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        Integer value = getInteger(key, defaultValue);
        if (value != null && value <= 0) {
            return defaultValue;
        }
        return value;
    }

    /**
     * 获取短整形正整数参数值
     *
     * @param key          参数名称
     * @param defaultValue 默认值
     * @return 参数值
     */
    public Short getPositive(final String key, final Short defaultValue) {
        if (defaultValue != null && defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        Short value = getShort(key, defaultValue);
        if (value != null && value <= 0) {
            return defaultValue;
        }
        return value;
    }

    /**
     * 获取字节正整数参数值
     *
     * @param key          参数名称
     * @param defaultValue 默认值
     * @return 参数值
     */
    public Byte getPositive(final String key, final Byte defaultValue) {
        if (defaultValue != null && defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        Byte value = getByte(key, defaultValue);
        if (value != null && value <= 0) {
            return defaultValue;
        }
        return value;
    }


    /**
     * 判断参数是否存在
     *
     * @param key 参数名称
     * @return
     * <ul>
     * <li>true 存在</li>
     * <li>false 不存在</li>
     * </ul>
     */
    public boolean hasParameter(final String key) {
        String value = getString(key);
        return value != null && !value.isEmpty();
    }

    /**
     * 添加布尔参数
     *
     * @param key   参数名称
     * @param value 值
     * @return 新创建的URL对象
     */
    public URL add(final String key, final boolean value) {
        return add(key, String.valueOf(value));
    }

    /**
     * 添加字符参数
     *
     * @param key   参数名称
     * @param value 值
     * @return 新创建的URL对象
     */
    public URL add(final String key, final char value) {
        return add(key, String.valueOf(value));
    }

    /**
     * 添加字节参数
     *
     * @param key   参数名称
     * @param value 值
     * @return 新创建的URL对象
     */
    public URL add(final String key, final byte value) {
        return add(key, String.valueOf(value));
    }

    /**
     * 添加短整数参数
     *
     * @param key   参数名称
     * @param value 值
     * @return 新创建的URL对象
     */
    public URL add(final String key, final short value) {
        return add(key, String.valueOf(value));
    }

    /**
     * 添加整数参数
     *
     * @param key   参数名称
     * @param value 值
     * @return 新创建的URL对象
     */
    public URL add(final String key, final int value) {
        return add(key, String.valueOf(value));
    }

    /**
     * 添加长整数参数
     *
     * @param key   参数名称
     * @param value 值
     * @return 新创建的URL对象
     */
    public URL add(final String key, final long value) {
        return add(key, String.valueOf(value));
    }

    /**
     * 添加单精度浮点数参数
     *
     * @param key   参数名称
     * @param value 值
     * @return 新创建的URL对象
     */
    public URL add(final String key, final float value) {
        return add(key, String.valueOf(value));
    }

    /**
     * 添加双精度浮点数参数
     *
     * @param key   参数名称
     * @param value 值
     * @return 新创建的URL对象
     */
    public URL add(final String key, final double value) {
        return add(key, String.valueOf(value));
    }

    /**
     * 添加数字参数
     *
     * @param key   参数名称
     * @param value 值
     * @return 新创建的URL对象
     */
    public URL add(final String key, final Number value) {
        if (value == null) {
            return this;
        }
        return add(key, String.valueOf(value));
    }

    /**
     * 添加字符序列参数
     *
     * @param key   参数名称
     * @param value 值
     * @return 新创建的URL对象
     */
    public URL add(final String key, final CharSequence value) {
        if (value == null || value.length() == 0) {
            return this;
        }
        return add(key, String.valueOf(value));
    }

    /**
     * 添加字符串参数
     *
     * @param key   参数名称
     * @param value 值
     * @return 新创建的URL对象
     */
    public URL add(final String key, final String value) {
        if (key == null || key.isEmpty() || value == null || value.isEmpty()) {
            return this;
        }
        Map<String, String> map = new HashMap<String, String>(getParameters());
        map.put(key, value);
        return new URL(protocol, username, password, host, port, path, map);
    }

    /**
     * 添加字符串参数
     *
     * @param key    参数名称
     * @param value  值
     * @param encode 是否编码
     * @return 新创建的URL对象
     */
    public URL add(final String key, final String value, final boolean encode) {
        if (!encode) {
            return add(key, value);
        }
        return add(key, encode(value));
    }

    /**
     * 如果参数不存在，则添加字符串参数
     *
     * @param key   参数名称
     * @param value 值
     * @return 新创建的URL对象
     */
    public URL addIfAbsent(final String key, final String value) {
        if (key == null || key.isEmpty() || value == null || value.isEmpty()) {
            return this;
        }
        if (hasParameter(key)) {
            return this;
        }
        Map<String, String> map = new HashMap<String, String>(getParameters());
        map.put(key, value);
        return new URL(protocol, username, password, host, port, path, map);
    }

    /**
     * 添加参数
     *
     * @param parameters 参数
     * @return 新创建的URL对象
     */
    public URL add(final Map<String, String> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return this;
        }
        Map<String, String> map = new HashMap<String, String>(getParameters());
        map.putAll(parameters);
        return new URL(protocol, username, password, host, port, path, map);
    }

    /**
     * 添加不存在的参数
     *
     * @param parameters 参数
     * @return 新创建的URL对象
     */
    public URL addIfAbsent(final Map<String, String> parameters) {
        if (parameters == null || parameters.size() == 0) {
            return this;
        }
        Map<String, String> map = new HashMap<String, String>(parameters);
        map.putAll(getParameters());
        return new URL(protocol, username, password, host, port, path, map);
    }

    /**
     * 删除参数
     *
     * @param key 参数
     * @return 新的URL对象
     */
    public URL remove(final String key) {
        if (key == null || key.isEmpty()) {
            return this;
        }
        return remove(key);
    }

    /**
     * 删除参数
     *
     * @param keys 参数
     * @return 新的URL对象
     */
    public URL remove(final Collection<String> keys) {
        if (keys == null || keys.size() == 0) {
            return this;
        }
        return remove(keys.toArray(new String[keys.size()]));
    }

    /**
     * 删除参数
     *
     * @param keys 参数
     * @return 新的URL对象
     */
    public URL remove(final String... keys) {
        if (keys == null || keys.length == 0) {
            return this;
        }
        Map<String, String> parameters = getParameters();
        Map<String, String> map = new HashMap<String, String>(parameters);
        for (String key : keys) {
            map.remove(key);
        }
        if (map.size() == parameters.size()) {
            return this;
        }
        return new URL(protocol, username, password, host, port, path, map);
    }

    /**
     * 删除所有参数
     *
     * @return 新的URL对象
     */
    public URL remove() {
        return new URL(protocol, username, password, host, port, path, new HashMap<String, String>());
    }

    /**
     * 转换成字符串，不包括用户信息
     *
     * @return 字符串表示
     */
    public String toString() {
        return toString(false, true); // no show username and password
    }

    /**
     * 构建字符串
     *
     * @param user       是否要带用户
     * @param parameter  是否要带参数
     * @param parameters 指定参数
     * @return 字符串
     */
    public String toString(final boolean user, final boolean parameter, final String... parameters) {
        StringBuilder buf = new StringBuilder();
        if (protocol != null && !protocol.isEmpty()) {
            buf.append(protocol).append("://");
        }
        if (user && username != null && !username.isEmpty()) {
            buf.append(username);
            if (password != null && !password.isEmpty()) {
                buf.append(':').append(password);
            }
            buf.append('@');
        }
        if (host != null && !host.isEmpty()) {
            buf.append(host);
            if (port > 0) {
                buf.append(':').append(port);
            }
        }
        if (path != null && !path.isEmpty()) {
            if (path.charAt(0) != '/') {
                buf.append('/');
            }
            buf.append(path);
        }
        if (parameter) {
            appand(buf, true, parameters);
        }
        return buf.toString();
    }

    /**
     * 获取参数字符串
     *
     * @param concat     是否追加参数连接符号"?"
     * @param parameters 参数名称
     * @return 参数
     */
    public String toParameter(final boolean concat, final String[] parameters) {
        StringBuilder buf = new StringBuilder();
        appand(buf, concat, parameters);
        return buf.toString();
    }

    /**
     * 追加参数
     *
     * @param buf        缓冲器
     * @param concat     是否追加参数连接符号"?"
     * @param parameters 参数名称
     */
    private void appand(final StringBuilder buf, final boolean concat, final String[] parameters) {
        Map<String, String> map = getParameters();
        if (map != null && !map.isEmpty()) {
            Set<String> includes = (parameters == null || parameters.length == 0 ? null : new HashSet<String>(
                    Arrays.asList(parameters)));
            boolean first = true;
            String key;
            // 按照字符串排序
            for (Map.Entry<String, String> entry : new TreeMap<String, String>(map).entrySet()) {
                key = entry.getKey();
                if (key != null && key.length() > 0 && (includes == null || includes.contains(key))) {
                    if (first) {
                        if (concat) {
                            buf.append('?');
                        }
                        first = false;
                    } else {
                        buf.append('&');
                    }
                    buf.append(key).append('=');
                    if (entry.getValue() != null) {
                        buf.append(entry.getValue().trim());
                    }
                }
            }
        }
    }

    public InetSocketAddress toInetSocketAddress() {
        return new InetSocketAddress(host, port);
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((host == null) ? 0 : host.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + port;
        result = prime * result + ((protocol == null) ? 0 : protocol.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        URL other = (URL) obj;
        if (host == null) {
            if (other.host != null) {
                return false;
            }
        } else if (!host.equals(other.host)) {
            return false;
        }
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        if (port != other.port) {
            return false;
        }
        if (protocol == null) {
            return other.protocol == null;
        } else return protocol.equals(other.protocol);
    }
}
