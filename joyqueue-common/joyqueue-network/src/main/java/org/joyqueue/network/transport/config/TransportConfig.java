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
package org.joyqueue.network.transport.config;

import org.joyqueue.toolkit.network.IpUtil;
import org.joyqueue.toolkit.os.Systems;
import org.joyqueue.toolkit.retry.RetryPolicy;


/**
 * TransportConfig
 *
 * author: gaohaoxiang
 * date: 2018/8/13
 */
public class TransportConfig {

    // 绑定地址
    private  String host = IpUtil.getLocalIp();
    // 接受请求线程数
    private  int acceptThread = 1;
    private String acceptThreadName = "accept-eventLoop";
    // io线程数
    private  int ioThread = Runtime.getRuntime().availableProcessors() * 2;
    private String ioThreadName = "io-eventLoop";
    // 通道最大空闲时间(毫秒)
    private  int maxIdleTime = 120 * 1000;
    // 表示是否允许重用Socket所绑定的本地地址
    private  boolean reuseAddress = true;
    // 关闭时候，对未发送数据包等待时间(秒)，-1,0:禁用,丢弃未发送的数据包;>0，等到指定时间，如果还未发送则丢弃
    private  int soLinger = -1;
    // 启用nagle算法，为真立即发送，否则得到确认或缓冲区满发送
    private  boolean tcpNoDelay = true;
    // 保持活动连接，定期心跳包
    private  boolean keepAlive = true;
    // socket读超时时间(毫秒)
    private  int soTimeout = 2000;
    // socket缓冲区大小
    private  int socketBufferSize = 1024 * 16;
    // 数据包最大大小
    private  int frameMaxSize = 1024 * 1024 * 4 + 1024;
    // 连接请求最大队列长度，如果队列满时收到连接指示，则拒绝该连接。
    private  int backlog = 65536;
    // 最大单向请求并发数
    private  int maxOneway = 10240;
    // 非阻塞oneway
    private boolean nonBlockOneway = false;
    // 非阻塞异步
    private boolean nonBlockAsync = false;
    // 最大异步请求数
    private  int maxAsync = 10240;
    // 异步回调线程数量
    private int callbackThreads = Systems.getCores();
    // 默认发送数据包超时时间
    private int sendTimeout = 1000 * 1;

    /*** 重试策略配置 ***/
    // 最大重试次数(无限制)
    private int  maxRetrys = 2;
    // 最大重试间隔(默认5分钟)
    private int maxRetryDelay = 0;
    // 重试间隔
    private int retryDelay = 1000 * 1;
    // 指数增加间隔时间
    private boolean useExponentialBackOff = false;
    // 指数系数，必须>=1
    private double backOffMultiplier = 2.0;
    // 过期时间（默认3天）
    private int expireTime = 0;
    // 重试
    private RetryPolicy retryPolicy;
    // 清理间隔
    private int clearInterval = 1000 * 1;


    public RetryPolicy getRetryPolicy() {
        if (this.retryPolicy == null) {
            this.retryPolicy = new RetryPolicy(getRetryDelay(), getMaxRetryDelay(), getMaxRetrys(), isUseExponentialBackOff(), getBackOffMultiplier(), getExpireTime());
        }
        return retryPolicy;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getAcceptThread() {
        return acceptThread;
    }

    public void setAcceptThread(int acceptThread) {
        this.acceptThread = acceptThread;
    }

    public int getIoThread() {
        return ioThread;
    }

    public void setIoThread(int ioThread) {
        this.ioThread = ioThread;
    }

    public int getMaxIdleTime() {
        return maxIdleTime;
    }

    public void setMaxIdleTime(int maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
    }

    public boolean isReuseAddress() {
        return reuseAddress;
    }

    public void setReuseAddress(boolean reuseAddress) {
        this.reuseAddress = reuseAddress;
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

    public int getFrameMaxSize() {
        return frameMaxSize;
    }

    public void setFrameMaxSize(int frameMaxSize) {
        this.frameMaxSize = frameMaxSize;
    }

    public int getBacklog() {
        return backlog;
    }

    public void setBacklog(int backlog) {
        this.backlog = backlog;
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

    public int getCallbackThreads() {
        return callbackThreads;
    }

    public void setCallbackThreads(int callbackThreads) {
        this.callbackThreads = callbackThreads;
    }

    public int getSendTimeout() {
        return sendTimeout;
    }

    public void setSendTimeout(int sendTimeout) {
        this.sendTimeout = sendTimeout;
    }

    public int getMaxRetrys() {
        return maxRetrys;
    }

    public void setMaxRetrys(int maxRetrys) {
        this.maxRetrys = maxRetrys;
    }

    public int getMaxRetryDelay() {
        return maxRetryDelay;
    }

    public void setMaxRetryDelay(int maxRetryDelay) {
        this.maxRetryDelay = maxRetryDelay;
    }

    public int getRetryDelay() {
        return retryDelay;
    }

    public void setRetryDelay(int retryDelay) {
        this.retryDelay = retryDelay;
    }

    public boolean isUseExponentialBackOff() {
        return useExponentialBackOff;
    }

    public void setUseExponentialBackOff(boolean useExponentialBackOff) {
        this.useExponentialBackOff = useExponentialBackOff;
    }

    public double getBackOffMultiplier() {
        return backOffMultiplier;
    }

    public void setBackOffMultiplier(double backOffMultiplier) {
        this.backOffMultiplier = backOffMultiplier;
    }

    public int getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(int expireTime) {
        this.expireTime = expireTime;
    }

    public void setRetryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    public void setNonBlockOneway(boolean nonBlockOneway) {
        this.nonBlockOneway = nonBlockOneway;
    }

    public boolean isNonBlockOneway() {
        return nonBlockOneway;
    }

    public void setNonBlockAsync(boolean nonBlockAsync) {
        this.nonBlockAsync = nonBlockAsync;
    }

    public boolean isNonBlockAsync() {
        return nonBlockAsync;
    }

    public void setAcceptThreadName(String acceptThreadName) {
        this.acceptThreadName = acceptThreadName;
    }

    public String getAcceptThreadName() {
        return acceptThreadName;
    }

    public void setIoThreadName(String ioThreadName) {
        this.ioThreadName = ioThreadName;
    }

    public String getIoThreadName() {
        return ioThreadName;
    }

    public void setClearInterval(int clearInterval) {
        this.clearInterval = clearInterval;
    }

    public int getClearInterval() {
        return clearInterval;
    }
}