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
package org.joyqueue.network.session;

import com.google.common.collect.Maps;
import org.joyqueue.network.transport.Transport;

import java.util.concurrent.ConcurrentMap;

/**
 * 连接会话
 */
public class Connection {
    // 通道
    private Transport transport;
    // 连接ID
    private String id;
    // 应用
    private String app;
    // 版本
    private String version;
    // 语言
    private Language language = Language.JAVA;
    // 客户端来源
    private String source;
    // 客户端地址
    private byte[] address;
    // 客户端地址字符串
    private String addressStr;
    // 客户端host
    private String host;
    // 服务端地址
    private byte[] serverAddress;
    // 区域
    private String region;
    // 作用域
    private String namespace;
    // 关联的producer
    private ConcurrentMap<String /** app **/, ConcurrentMap<String /** topic **/, String /** id **/>> producers = Maps.newConcurrentMap();
    // 关联的consumer
    private ConcurrentMap<String /** app **/, ConcurrentMap<String /** topic **/, String /** id **/>> consumers = Maps.newConcurrentMap();
    // 创建时间
    private long createTime;
    // 是否系统连接
    private boolean isSystem;
    // 是否认证
    private boolean isAuth;

    public Connection() {
    }

    public Connection(String id, String app, String version, Language language, byte[] address, byte[] serverAddress) {
        this.id = id;
        this.app = app;
        this.version = version;
        this.language = language;
        this.address = address;
        this.serverAddress = serverAddress;
    }

    public boolean isAuthorized(String app) {
        // TODO group处理
        if (app.contains(".")) {
            app = app.split("\\.")[0];
        }
        return (this.app.equals(app) || isSystem);
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApp() {
        return this.app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Language getLanguage() {
        return this.language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public byte[] getAddress() {
        return this.address;
    }

    public void setAddress(byte[] address) {
        this.address = address;
    }

    public void setAddressStr(String addressStr) {
        this.addressStr = addressStr;
    }

    public String getAddressStr() {
        return addressStr;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public byte[] getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(byte[] serverAddress) {
        this.serverAddress = serverAddress;
    }

    public Transport getTransport() {
        return transport;
    }

    public void setTransport(Transport transport) {
        this.transport = transport;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getRegion() {
        return region;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getNamespace() {
        return namespace;
    }

    public boolean addProducer(String topic, String app, String id) {
        return getOrCreateProducers(app).putIfAbsent(topic, id) == null;
    }

    public String getProducer(String topic, String app) {
        return getOrCreateProducers(app).get(topic);
    }

    public boolean containsProducer(String topic, String app) {
        return getOrCreateProducers(app).containsKey(topic);
    }

    public boolean removeProducer(String topic, String app) {
        return getOrCreateProducers(app).remove(topic) != null;
    }

    public boolean addConsumer(String topic, String app, String id) {
        return getOrCreateConsumers(app).putIfAbsent(topic, id) == null;
    }

    public String getConsumer(String topic, String app) {
        return getOrCreateConsumers(app).get(topic);
    }

    public boolean containsConsumer(String topic, String app) {
        return getOrCreateConsumers(app).containsKey(topic);
    }

    public boolean removeConsumer(String topic, String app) {
        return getOrCreateConsumers(app).remove(topic) != null;
    }

    public ConcurrentMap<String, ConcurrentMap<String, String>> getProducers() {
        return producers;
    }

    public ConcurrentMap<String, ConcurrentMap<String, String>> getConsumers() {
        return consumers;
    }

    protected ConcurrentMap<String, String> getOrCreateProducers(String app) {
        ConcurrentMap<String, String> topicMap = producers.get(app);
        if (topicMap == null) {
            topicMap = Maps.newConcurrentMap();
            ConcurrentMap<String, String> oldTopicMap = producers.putIfAbsent(app, topicMap);
            if (oldTopicMap != null) {
                topicMap = oldTopicMap;
            }
        }
        return topicMap;
    }

    protected ConcurrentMap<String, String> getOrCreateConsumers(String app) {
        ConcurrentMap<String, String> topicMap = consumers.get(app);
        if (topicMap == null) {
            topicMap = Maps.newConcurrentMap();
            ConcurrentMap<String, String> oldTopicMap = consumers.putIfAbsent(app, topicMap);
            if (oldTopicMap != null) {
                topicMap = oldTopicMap;
            }
        }
        return topicMap;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getCreateTime() {
        return createTime;
    }

    public boolean isSystem() {
        return isSystem;
    }

    public void setSystem(boolean system) {
        isSystem = system;
    }

    public void setAuth(boolean auth) {
        isAuth = auth;
    }

    public boolean isAuth() {
        return isAuth;
    }
}