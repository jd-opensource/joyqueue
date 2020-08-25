package org.joyqueue.nsr.sql.operator.support;

import org.joyqueue.nsr.sql.operator.BatchSQLOperator;
import org.joyqueue.nsr.sql.operator.DataSourceFactory;
import org.joyqueue.nsr.sql.operator.ResultSet;
import org.joyqueue.nsr.sql.operator.SQLOperator;
import org.joyqueue.nsr.sql.util.DBUtils;
import org.joyqueue.toolkit.service.Service;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * DefaultSQLOperator
 * author: gaohaoxiang
 * date: 2020/8/12
 */
public class DefaultSQLOperator extends Service implements SQLOperator {

    private DataSourceFactory dataSourceFactory;

    private DataSource dataSource;

    public DefaultSQLOperator(Properties properties, DataSourceFactory dataSourceFactory) {
        this.dataSourceFactory = dataSourceFactory;
        this.dataSource = dataSourceFactory.createDataSource(properties);
    }

    @Override
    public String insert(String sql, Object... params) {
        Connection connection = getConnection();
        try {
            return DBUtils.insert(connection, sql, params);
        } finally {
            releaseConnection(connection);
        }
    }

    @Override
    public int update(String sql, Object... params) {
        Connection connection = getConnection();
        try {
            return DBUtils.update(connection, sql, params);
        } finally {
            releaseConnection(connection);
        }
    }

    @Override
    public int delete(String sql, Object... params) {
        Connection connection = getConnection();
        try {
            return DBUtils.delete(connection, sql, params);
        } finally {
            releaseConnection(connection);
        }
    }

    @Override
    public ResultSet query(String sql, Object... params) {
        Connection connection = getConnection();
        try {
            List<Map<String, String>> rows = DBUtils.query(connection, sql, params);
            return new ResultSet(rows);
        } finally {
            releaseConnection(connection);
        }
    }

    @Override
    public BatchSQLOperator beginBatch() {
        Connection connection = getTransactionConnection();
        return new DefaultBatchSQLOperator(connection);
    }

    @Override
    protected void doStop() {
        if (dataSource instanceof Closeable) {
            try {
                ((Closeable) dataSource).close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected Connection getTransactionConnection() {
        try {
            Connection connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            return connection;
        } catch (java.sql.SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (java.sql.SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected void releaseConnection(Connection connection) {
        try {
            connection.close();
        } catch (java.sql.SQLException e) {
            throw new RuntimeException();
        }
    }
}