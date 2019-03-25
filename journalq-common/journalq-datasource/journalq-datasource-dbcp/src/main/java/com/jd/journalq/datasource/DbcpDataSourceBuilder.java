package com.jd.journalq.datasource;


import java.sql.Connection;

/**
 * DBCP数据源构造器
 */
public class DbcpDataSourceBuilder implements DataSourceBuilder {


    @Override
    public XDataSource build(DataSourceConfig config) {
        XBasicDataSource dataSource = new XBasicDataSource();
        dataSource.setDriverClassName(config.getDriver());
        dataSource.setUrl(config.getUrl());
        dataSource.setUsername(config.getUser());
        dataSource.setPassword(config.getPassword());
        dataSource.setMaxActive(config.getMaxPoolSize());
        dataSource.setMinIdle(config.getMinIdle());
        dataSource.setMaxIdle(config.getMinIdle());
        if (config.getConnectionTimeout() > 0) {
            dataSource.setMaxWait(config.getConnectionTimeout());
        }
        dataSource.setValidationQuery(config.getValidationQuery());
        dataSource.setConnectionProperties(config.getConnectionProperties());
        if (config.getMaxLifetime() > 0) {
            dataSource.setRemoveAbandoned(true);
            dataSource.setRemoveAbandonedTimeout((int) config.getMaxLifetime());
        }
        if (config.getIdleTimeout() > 0) {
            // 启用空闲清理线程
            dataSource.setTimeBetweenEvictionRunsMillis(config.getCleanupInterval());
            dataSource.setMinEvictableIdleTimeMillis(config.getIdleTimeout());
            if (config.getValidationQuery() != null && !config.getValidationQuery().isEmpty()) {
                dataSource.setTestWhileIdle(true);
            }
        }
        dataSource.setDefaultAutoCommit(config.isAutoCommit());
        dataSource.setDefaultReadOnly(config.isAutoCommit());
        dataSource.setDefaultCatalog(config.getCatalog());
        // 设置事务
        if (config.TRANSACTION_READ_COMMITTED.equals(config.getTransactionIsolation())) {
            dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        } else if (config.TRANSACTION_READ_COMMITTED1.equals(config.getTransactionIsolation())) {
            dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        } else if (config.TRANSACTION_REPEATABLE_READ.equals(config.getTransactionIsolation())) {
            dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
        } else if (config.TRANSACTION_SERIALIZABLE.equals(config.getTransactionIsolation())) {
            dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        } else if (config.TRANSACTION_NONE.equals(config.getTransactionIsolation())) {
            dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_NONE);
        }
        return dataSource;
    }

    @Override
    public Object type() {
        return "dbcp";
    }
}
