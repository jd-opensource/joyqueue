package org.joyqueue.nsr.sql.operator;

import java.util.List;

/**
 * BatchSQLOperator
 * author: gaohaoxiang
 * date: 2020/8/12
 */
public interface BatchSQLOperator {

    void insert(String sql, Object... params);

    void update(String sql, Object... params);

    void delete(String sql, Object... params);

    List<Object> commit();

    void rollback();
}