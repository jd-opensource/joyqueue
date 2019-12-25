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
package org.joyqueue.model.domain;

public class Client {
    private static final long serialVersionUID = -2261906559879545877L;
    // 连接ID
    private String connectionId;
    // 应用
    private String app;
    // 版本号
    private String version;
    // 语言
    private String language;
    // 客户端地址
    private String ip;
    // 客户端端口
    private int port;
    // 是否是生产者
    private boolean producerRole = false;
    // 是否是消费者
    private boolean consumerRole = false;

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isProducerRole() {
        return producerRole;
    }

    public void setProducerRole(boolean producerRole) {
        this.producerRole = producerRole;
    }

    public boolean isConsumerRole() {
        return consumerRole;
    }

    public void setConsumerRole(boolean consumerRole) {
        this.consumerRole = consumerRole;
    }
}
