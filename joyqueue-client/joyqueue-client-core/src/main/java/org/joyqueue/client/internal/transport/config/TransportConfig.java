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
package org.joyqueue.client.internal.transport.config;

import org.joyqueue.toolkit.retry.RetryPolicy;

/**
 * TransportConfig
 *
 * author: gaohaoxiang
 * date: 2018/11/28
 */
public class TransportConfig {

    // 连接数
    private int connections = 1;
    // 超时
    private int sendTimeout = 1000 * 5;
    // io线程数
    private int ioThreads = Runtime.getRuntime().availableProcessors() * 4;
    // 回调线程数
    private int callbackThreads = Runtime.getRuntime().availableProcessors();
    // 最大空闲
    private int channelMaxIdleTime = 1000 * 60 * 10;
    // heartbeat
    private int heartbeatInterval = 1000 * 5;
    // heartbeat timeout
    private int heartbeatTimeout = 1000 * 3;
    // heartbeat maxIdle
    private int heartbeatMaxIdleTime = 1000 * 60 * 1;
    // 关闭时候，对未发送数据包等待时间(秒)，-1,0:禁用,丢弃未发送的数据包;>0，等到指定时间，如果还未发送则丢弃
    private int soLinger = -1;
    // 启用nagle算法，为真立即发送，否则得到确认或缓冲区满发送
    private boolean tcpNoDelay = true;
    // 保持活动连接，定期心跳包
    private boolean keepAlive = true;
    // socket读超时时间(毫秒)
    private int soTimeout = 1000 * 2;
    // socket缓冲区大小
    private int socketBufferSize = 1024 * 1024 * 1;
    // 最大单向请求并发数
    private int maxOneway = 256;
    // 最大异步请求数
    private int maxAsync = 128;
    // 非阻塞oneway
    private boolean nonBlockOneway = false;
    // 重试
    private RetryPolicy retryPolicy = new RetryPolicy(1000 * 1, 1000 * 60, 2, false, 2.0, 0);

    public TransportConfig copy() {
        TransportConfig transportConfig = new TransportConfig();
        transportConfig.setConnections(connections);
        transportConfig.setSendTimeout(sendTimeout);
        transportConfig.setIoThreads(ioThreads);
        transportConfig.setCallbackThreads(callbackThreads);
        transportConfig.setHeartbeatInterval(heartbeatInterval);
        transportConfig.setHeartbeatTimeout(heartbeatTimeout);
        transportConfig.setHeartbeatMaxIdleTime(heartbeatMaxIdleTime);
        transportConfig.setChannelMaxIdleTime(channelMaxIdleTime);
        transportConfig.setSoLinger(soLinger);
        transportConfig.setTcpNoDelay(tcpNoDelay);
        transportConfig.setKeepAlive(keepAlive);
        transportConfig.setSoTimeout(soTimeout);
        transportConfig.setSocketBufferSize(socketBufferSize);
        transportConfig.setMaxOneway(maxOneway);
        transportConfig.setMaxAsync(maxAsync);
        transportConfig.setRetryPolicy(retryPolicy);
        transportConfig.setNonBlockOneway(nonBlockOneway);
        return transportConfig;
    }

    public RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    public void setRetryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    public void setConnections(int connections) {
        this.connections = connections;
    }

    public int getConnections() {
        return connections;
    }

    public int getSendTimeout() {
        return sendTimeout;
    }

    public void setSendTimeout(int sendTimeout) {
        this.sendTimeout = sendTimeout;
    }

    public int getIoThreads() {
        return ioThreads;
    }

    public void setIoThreads(int ioThreads) {
        this.ioThreads = ioThreads;
    }

    public int getCallbackThreads() {
        return callbackThreads;
    }

    public void setCallbackThreads(int callbackThreads) {
        this.callbackThreads = callbackThreads;
    }

    public void setHeartbeatInterval(int heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatTimeout(int heartbeatTimeout) {
        this.heartbeatTimeout = heartbeatTimeout;
    }

    public int getHeartbeatTimeout() {
        return heartbeatTimeout;
    }

    public void setHeartbeatMaxIdleTime(int heartbeatMaxIdleTime) {
        this.heartbeatMaxIdleTime = heartbeatMaxIdleTime;
    }

    public int getHeartbeatMaxIdleTime() {
        return heartbeatMaxIdleTime;
    }

    public int getChannelMaxIdleTime() {
        return channelMaxIdleTime;
    }

    public void setChannelMaxIdleTime(int channelMaxIdleTime) {
        this.channelMaxIdleTime = channelMaxIdleTime;
    }

    public int getSoLinger() {
        return soLinger;
    }

    public void setSoLinger(int soLinger) {
        this.soLinger = soLinger;
    }

    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public int getSoTimeout() {
        return soTimeout;
    }

    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }

    public int getSocketBufferSize() {
        return socketBufferSize;
    }

    public void setSocketBufferSize(int socketBufferSize) {
        this.socketBufferSize = socketBufferSize;
    }

    public int getMaxOneway() {
        return maxOneway;
    }

    public void setMaxOneway(int maxOneway) {
        this.maxOneway = maxOneway;
    }

    public int getMaxAsync() {
        return maxAsync;
    }

    public void setMaxAsync(int maxAsync) {
        this.maxAsync = maxAsync;
    }

    public void setNonBlockOneway(boolean nonBlockOneway) {
        this.nonBlockOneway = nonBlockOneway;
    }

    public boolean isNonBlockOneway() {
        return nonBlockOneway;
    }
}