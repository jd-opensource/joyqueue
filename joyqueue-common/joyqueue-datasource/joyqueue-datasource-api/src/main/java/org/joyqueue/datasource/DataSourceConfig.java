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
package org.joyqueue.datasource;

/**
 * 连接池配置
 */
public class DataSourceConfig {
    public static final String TRANSACTION_READ_COMMITTED = "TRANSACTION_READ_COMMITTED";
    public static final String TRANSACTION_READ_COMMITTED1 = "TRANSACTION_READ_COMMITTED";
    public static final String TRANSACTION_REPEATABLE_READ = "TRANSACTION_REPEATABLE_READ";
    public static final String TRANSACTION_SERIALIZABLE = "TRANSACTION_SERIALIZABLE";
    public static final String TRANSACTION_NONE = "TRANSACTION_NONE";
    // 连接池类型
    private String type = "HikariCP";
    // 驱动
    private String driver;
    // 连接URL
    private String url;
    // 用户
    private String user;
    // 密码
    private String password;
    // 等待连接超时时间
    private long connectionTimeout = 1000;
    // 空闲超时时间
    private long idleTimeout = 600000;
    // 连接生命周期
    private long maxLifetime = 1800000;
    // 连接池大小
    private int maxPoolSize = 20;
    // 最小空闲连接数
    private int minIdle = 5;
    // 清理时间间隔
    private long cleanupInterval = 1000 * 60 * 5;
    // 验证连接
    private String validationQuery;
    // 连接属性
    private String connectionProperties;
    // 自动提交
    private boolean autoCommit = true;
    // 事务隔离级别
    private String transactionIsolation;
    // 只读
    private boolean readOnly = false;
    // 数据库
    private String catalog;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public long getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(long idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public long getMaxLifetime() {
        return maxLifetime;
    }

    public void setMaxLifetime(long maxLifetime) {
        this.maxLifetime = maxLifetime;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    public long getCleanupInterval() {
        return cleanupInterval;
    }

    public void setCleanupInterval(long cleanupInterval) {
        if (cleanupInterval > 0) {
            this.cleanupInterval = cleanupInterval;
        }
    }

    public String getValidationQuery() {
        return validationQuery;
    }

    public void setValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
    }

    public String getConnectionProperties() {
        return connectionProperties;
    }

    public void setConnectionProperties(String connectionProperties) {
        this.connectionProperties = connectionProperties;
    }

    public boolean isAutoCommit() {
        return autoCommit;
    }

    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    public String getTransactionIsolation() {
        return transactionIsolation;
    }

    public void setTransactionIsolation(String transactionIsolation) {
        this.transactionIsolation = transactionIsolation;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }
}
