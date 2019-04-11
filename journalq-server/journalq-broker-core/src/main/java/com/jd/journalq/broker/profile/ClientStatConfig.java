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
