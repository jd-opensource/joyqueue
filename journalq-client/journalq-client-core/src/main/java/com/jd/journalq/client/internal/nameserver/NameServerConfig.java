/**
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
package com.jd.journalq.client.internal.nameserver;

import org.apache.commons.lang3.StringUtils;

/**
 * NameServerConfig
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/29
 */
public class NameServerConfig {

    // 连接地址
    private String address;
    // 区域
    private String region = StringUtils.EMPTY;
    // 作用域
    private String namespace = StringUtils.EMPTY;
    // 用户名
    @Deprecated
    private String username;
    // 密码
    @Deprecated
    private String password;
    // 应用
    private String app;
    // 令牌
    private String token;
    // 更新元数据间隔
    private int updateMetadataInterval = 1000 * 60 * 1;
    // 临时元数据更新间隔
    private int tempMetadataInterval = 1000 * 5;
    // 更新元数据线程数
    private int updateMetadataThread = 1;
    // 更新元数据大小
    private int updateMetadataQueueSize = 10240;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
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

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getUpdateMetadataInterval() {
        return updateMetadataInterval;
    }

    public void setUpdateMetadataInterval(int updateMetadataInterval) {
        this.updateMetadataInterval = updateMetadataInterval;
    }

    public void setTempMetadataInterval(int tempMetadataInterval) {
        this.tempMetadataInterval = tempMetadataInterval;
    }

    public int getTempMetadataInterval() {
        return tempMetadataInterval;
    }

    public void setUpdateMetadataThread(int updateMetadataThread) {
        this.updateMetadataThread = updateMetadataThread;
    }

    public int getUpdateMetadataThread() {
        return updateMetadataThread;
    }

    public void setUpdateMetadataQueueSize(int updateMetadataQueueSize) {
        this.updateMetadataQueueSize = updateMetadataQueueSize;
    }

    public int getUpdateMetadataQueueSize() {
        return updateMetadataQueueSize;
    }

    @Override
    public String toString() {
        return "NameServerConfig{" +
                "address='" + address + '\'' +
                ", region='" + region + '\'' +
                ", namespace='" + namespace + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", app='" + app + '\'' +
                ", token='" + token + '\'' +
                ", updateMetadataInterval=" + updateMetadataInterval +
                '}';
    }
}