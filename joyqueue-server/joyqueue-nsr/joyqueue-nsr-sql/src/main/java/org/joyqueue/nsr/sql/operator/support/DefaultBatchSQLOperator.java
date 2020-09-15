package org.joyqueue.nsr.sql.operator.support;

import com.google.common.collect.Lists;
import org.joyqueue.nsr.sql.operator.BatchSQLOperator;
import org.joyqueue.nsr.sql.util.DBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * DefaultBatchSQLOperator
 * author: gaohaoxiang
 * date: 2020/8/12
 */
public class DefaultBatchSQLOperator implements BatchSQLOperator {

    protected static final Logger logger = LoggerFactory.getLogger(DefaultBatchSQLOperator.class);

    private Connection connection;
    List<Object> resultList = Lists.newArrayList();

    public DefaultBatchSQLOperator(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void insert(String sql, Object... params) {
        String result = DBUtils.insert(connection, sql, params);
        resultList.add(result);
    }

    @Override
    public void update(String sql, Object... params) {
        int result = DBUtils.update(connection, sql, params);
        resultList.add(result);
    }

    @Override
    public void delete(String sql, Object... params) {
        int result = DBUtils.delete(connection, sql, params);
        resultList.add(result);
    }

    @Override
    public List<Object> commit() {
        try {
            connection.commit();
            return resultList;
        } catch (SQLException e) {
            logger.error("commit transaction exception", e);
            throw new RuntimeException(e);
        } finally {
            try {
                connection.close();
            } catch (java.sql.SQLException e) {
                logger.error("close transaction exception", e);
            }
        }
    }

    @Override
    public void rollback() {
        try {
            connection.rollback();
        } catch (SQLException e) {
            logger.error("rollback transaction exception", e);
            throw new RuntimeException(e);
        } finally {
            try {
                connection.close();
            } catch (java.sql.SQLException e) {
                logger.error("close transaction exception", e);
            }
        }
    }
}