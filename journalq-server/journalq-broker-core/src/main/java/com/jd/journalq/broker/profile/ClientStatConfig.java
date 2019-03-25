package com.jd.journalq.broker.profile;


import com.jd.journalq.toolkit.config.Context;

import java.util.Map;

/**
 * 客户端性能存储配置
 * User: weiqisong
 * Date: 14-9-23
 * Time: 下午5:11
 */
public class ClientStatConfig extends Context {

    public static final String MONITOR_DAO = "monitor.dao";
    // 客户端性能数据存储类型
    protected String clientStatType = "hbase";
    // 缓存queue的大小。
    protected int queueSize = 1000;
    // 入队超时时间
    protected int enqueueTimeout = 50;

    public ClientStatConfig() {
        super(null);
    }

    public ClientStatConfig(Map<String, Object> parameters) {
        super(parameters);
    }

    public String getClientStatType() {
        return clientStatType;
    }

    public void setClientStatType(String clientStatType) {
        this.clientStatType = clientStatType;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    public int getEnqueueTimeout() {
        return enqueueTimeout;
    }

    public void setEnqueueTimeout(int enqueueTimeout) {
        this.enqueueTimeout = enqueueTimeout;
    }
}
