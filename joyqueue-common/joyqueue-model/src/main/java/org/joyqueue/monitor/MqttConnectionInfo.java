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
package org.joyqueue.monitor;

/**
 * @author majun8
 */
public class MqttConnectionInfo extends BaseMonitorInfo {

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
    private String ipAddress;
    private long createdTime;
    private long lastOperateTime;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
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

    public boolean isCleanSession() {
        return cleanSession;
    }

    public void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
    }

    public boolean isWillRetain() {
        return isWillRetain;
    }

    public void setWillRetain(boolean willRetain) {
        isWillRetain = willRetain;
    }

    public int getWillQos() {
        return willQos;
    }

    public void setWillQos(int willQos) {
        this.willQos = willQos;
    }

    public int getMqttVersion() {
        return mqttVersion;
    }

    public void setMqttVersion(int mqttVersion) {
        this.mqttVersion = mqttVersion;
    }

    public boolean isWillFlag() {
        return isWillFlag;
    }

    public void setWillFlag(boolean willFlag) {
        isWillFlag = willFlag;
    }

    public int getKeepAliveTimeSeconds() {
        return keepAliveTimeSeconds;
    }

    public void setKeepAliveTimeSeconds(int keepAliveTimeSeconds) {
        this.keepAliveTimeSeconds = keepAliveTimeSeconds;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
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
}
