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
package org.joyqueue.model;

import java.io.Serializable;

public class BrokerMetadata implements Serializable, Cloneable {
    private long id;
    private String address;
    private String ip;
    private int port;
    private int backEndPort;
    private int managerPort;
    private int monitorPort;
    private String permission;
    private String retryType;
    private String leaderAddress;
    private String leaderBrokerId;
    private String leaderRetryType;
    private String leaderPermission;
    private String leaderIp;
    private String leaderPort;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public int getBackEndPort() {
        return backEndPort;
    }

    public void setBackEndPort(int backEndPort) {
        this.backEndPort = backEndPort;
    }

    public int getManagerPort() {
        return managerPort;
    }

    public void setManagerPort(int managerPort) {
        this.managerPort = managerPort;
    }

    public int getMonitorPort() {
        return monitorPort;
    }

    public void setMonitorPort(int monitorPort) {
        this.monitorPort = monitorPort;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getRetryType() {
        return retryType;
    }

    public void setRetryType(String retryType) {
        this.retryType = retryType;
    }

    public String getLeaderAddress() {
        return leaderAddress;
    }

    public void setLeaderAddress(String leaderAddress) {
        this.leaderAddress = leaderAddress;
    }

    public String getLeaderBrokerId() {
        return leaderBrokerId;
    }

    public void setLeaderBrokerId(String leaderBrokerId) {
        this.leaderBrokerId = leaderBrokerId;
    }

    public String getLeaderRetryType() {
        return leaderRetryType;
    }

    public void setLeaderRetryType(String leaderRetryType) {
        this.leaderRetryType = leaderRetryType;
    }

    public String getLeaderPermission() {
        return leaderPermission;
    }

    public void setLeaderPermission(String leaderPermission) {
        this.leaderPermission = leaderPermission;
    }

    public String getLeaderIp() {
        return leaderIp;
    }

    public void setLeaderIp(String leaderIp) {
        this.leaderIp = leaderIp;
    }

    public String getLeaderPort() {
        return leaderPort;
    }

    public void setLeaderPort(String leaderPort) {
        this.leaderPort = leaderPort;
    }

}