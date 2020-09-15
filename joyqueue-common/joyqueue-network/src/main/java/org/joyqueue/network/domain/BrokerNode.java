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
package org.joyqueue.network.domain;

import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

/**
 * BrokerNode
 *
 * author: gaohaoxiang
 * date: 2018/11/29
 */
public class BrokerNode implements Serializable {

    private int id;
    private String host;
    private int port;
    private String dataCenter;
    private boolean nearby;
    private int weight;

    private int sysCode;
    private int permission;

    private ConcurrentMap<Object, Object> attachments = Maps.newConcurrentMap();

    public BrokerNode() {
    }

    public BrokerNode(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public BrokerNode(int id, String host, int port, String dataCenter, boolean nearby, int weight) {
        this.id = id;
        this.host = host;
        this.port = port;
        this.dataCenter = dataCenter;
        this.nearby = nearby;
        this.weight = weight;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDataCenter() {
        return dataCenter;
    }

    public void setDataCenter(String dataCenter) {
        this.dataCenter = dataCenter;
    }

    public void setNearby(boolean nearby) {
        this.nearby = nearby;
    }

    public boolean isNearby() {
        return nearby;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }

    public void setSysCode(int sysCode) {
        this.sysCode = sysCode;
    }

    public int getSysCode() {
        return sysCode;
    }

    public void setPermission(int permission) {
        this.permission = permission;
    }

    public int getPermission() {
        return permission;
    }

    public void setReadable(boolean readable) {
        this.permission = BrokerPermission.setReadable(permission, readable);
    }

    public void setWritable(boolean writable) {
        this.permission = BrokerPermission.setWritable(permission, writable);
    }

    public boolean isReadable() {
        return BrokerPermission.isReadable(permission);
    }

    public boolean isWritable() {
        return BrokerPermission.isWritable(permission);
    }

    public void setAttachments(ConcurrentMap<Object, Object> attachments) {
        this.attachments = attachments;
    }

    public ConcurrentMap<Object, Object> getAttachments() {
        return attachments;
    }

    public void putAttachment(Object key, Object value) {
        attachments.put(key, value);
    }

    public <T> T putIfAbsentAttachment(Object key, Object value) {
        return (T) attachments.putIfAbsent(key, value);
    }

    public <T> T getAttachment(Object key) {
        return (T) attachments.get(key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BrokerNode that = (BrokerNode) o;
        return id == that.id &&
                port == that.port &&
                host.equals(that.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, host, port);
    }

    @Override
    public String toString() {
        return "BrokerNode{" +
                "id=" + id +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", dataCenter='" + dataCenter + '\'' +
                ", nearby=" + nearby +
                ", weight=" + weight +
                '}';
    }
}