package org.joyqueue.nsr.sql.operator;

/**
 * SQLOperator
 * author: gaohaoxiang
 * date: 2020/8/12
 */
public interface SQLOperator {

    Object insert(String sql, Object... params);

    int update(String sql, Object... params);

    int delete(String sql, Object... params);

    ResultSet query(String sql, Object... params);

    BatchSQLOperator beginBatch();
}