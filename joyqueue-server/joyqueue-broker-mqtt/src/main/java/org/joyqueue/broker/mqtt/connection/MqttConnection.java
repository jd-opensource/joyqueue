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
package org.joyqueue.broker.mqtt.connection;

import org.joyqueue.network.session.Connection;
import org.joyqueue.toolkit.network.IpUtil;
import org.joyqueue.toolkit.time.SystemClock;
import io.netty.channel.Channel;

import java.util.Arrays;
import java.util.concurrent.ConcurrentMap;

/**
 * @author majun8
 */
public class MqttConnection extends Connection {
    private static final String SPLIT = ".";

    private String clientId;
    private String application;
    private String username;
    private String password;
    private String clientGroupName;
    private boolean cleanSession;
    private boolean isWillRetain;
    private int willQos;
    private int mqttVersion;
    private boolean isWillFlag;
    private int keepAliveTimeSeconds;
    private Channel channel;
    private long createdTime;
    private long lastOperateTime;

    public MqttConnection(String clientId, String username,
                          String password, boolean cleanSession,
                          int version, boolean isWillRetain,
                          int willQos, boolean isWillFlag,
                          int keepAliveTimeSeconds, Channel client) {
        this.clientId = clientId;
        setId(clientId);
        this.username = username;
        this.password = password;
        this.application = username;
        this.clientGroupName = application + SPLIT + clientId;
        this.cleanSession = cleanSession;
        this.mqttVersion = version;
        this.isWillRetain = isWillRetain;
        this.willQos = willQos;
        this.isWillFlag = isWillFlag;
        this.keepAliveTimeSeconds = keepAliveTimeSeconds;
        this.channel = client;
        this.createdTime = SystemClock.now();
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getClientGroupName() {
        return clientGroupName;
    }

    public void setClientGroupName(String clientGroupName) {
        this.clientGroupName = clientGroupName;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public boolean isCleanSession() {
        return cleanSession;
    }

    public int getKeepAliveTimeSeconds() {
        return keepAliveTimeSeconds;
    }

    public boolean isWillRetain() {
        return isWillRetain;
    }

    public int getWillQos() {
        return willQos;
    }

    public boolean isWillFlag() {
        return isWillFlag;
    }

    public void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
    }

    public void setWillRetain(boolean willRetain) {
        isWillRetain = willRetain;
    }

    public void setWillQos(int willQos) {
        this.willQos = willQos;
    }

    public void setWillFlag(boolean willFlag) {
        isWillFlag = willFlag;
    }

    public void setKeepAliveTimeSeconds(int keepAliveTimeSeconds) {
        this.keepAliveTimeSeconds = keepAliveTimeSeconds;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public int getMqttVersion() {
        return mqttVersion;
    }

    public void setMqttVersion(int mqttVersion) {
        this.mqttVersion = mqttVersion;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public long getLastOperateTime() {
        return lastOperateTime;
    }

    public void setLastOperateTime(long lastOperateTime) {
        this.lastOperateTime = lastOperateTime;
    }

    @Override
    public boolean addProducer(String topic, String app, String id) {
        return super.addProducer(topic, app, id);
    }

    @Override
    public String getProducer(String topic, String app) {
        return super.getProducer(topic, app);
    }

    @Override
    public boolean containsProducer(String topic, String app) {
        return super.containsProducer(topic, app);
    }

    @Override
    public boolean addConsumer(String topic, String app, String id) {
        return super.addConsumer(topic, app, id);
    }

    @Override
    public String getConsumer(String topic, String app) {
        return super.getConsumer(topic, app);
    }

    @Override
    public boolean containsConsumer(String topic, String app) {
        return super.containsConsumer(topic, app);
    }

    @Override
    public ConcurrentMap<String, ConcurrentMap<String, String>> getProducers() {
        return super.getProducers();
    }

    @Override
    public ConcurrentMap<String, ConcurrentMap<String, String>> getConsumers() {
        return super.getConsumers();
    }

    @Override
    protected ConcurrentMap<String, String> getOrCreateProducers(String app) {
        return super.getOrCreateProducers(app);
    }

    @Override
    protected ConcurrentMap<String, String> getOrCreateConsumers(String app) {
        return super.getOrCreateConsumers(app);
    }

    @Override
    public String toString() {
        return "MqttConnection{" +
                "clientId='" + clientId + '\'' +
                ", application='" + application + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", clientGroupName='" + clientGroupName + '\'' +
                ", cleanSession=" + cleanSession +
                ", isWillRetain=" + isWillRetain +
                ", willQos=" + willQos +
                ", mqttVersion=" + mqttVersion +
                ", isWillFlag=" + isWillFlag +
                ", keepAliveTimeSeconds=" + keepAliveTimeSeconds +
                ", channel=" + channel +
                ", createdTime=" + createdTime +
                ", lastOperateTime=" + lastOperateTime +
                ", address=" + IpUtil.toIp(getAddress()) +
                ", serverAddress=" + IpUtil.toIp(getServerAddress()) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        MqttConnection that = (MqttConnection) o;

        if (clientId != null && channel != null && getAddress() != null) {
            if (clientId != null ? !clientId.equals(that.clientId) : that.clientId != null)
                return false;
            if (channel != null ? !channel.equals(that.channel) : that.channel != null)
                return false;
            return getAddress() != null ? Arrays.hashCode(getAddress()) == Arrays.hashCode(that.getAddress()) : that.getAddress() == null;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int result = clientId != null ? clientId.hashCode() : 0;
        result = 31 * result + (channel != null ? channel.hashCode() : 0);
        result = 31 * result + (getAddress() != null ? Arrays.hashCode(getAddress()) : 0);
        return result;
    }
}
