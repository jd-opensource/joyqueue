package org.joyqueue.nsr.journalkeeper.operator;

import org.joyqueue.nsr.sql.operator.BatchSQLOperator;

import java.util.List;

/**
 * JournalkeeperBatchSQLOperator
 * author: gaohaoxiang
 * date: 2020/8/12
 */
public class JournalkeeperBatchSQLOperator implements BatchSQLOperator {

    private io.journalkeeper.sql.client.BatchSQLOperator journalkeeperBatchSQLOperator;

    public JournalkeeperBatchSQLOperator(io.journalkeeper.sql.client.BatchSQLOperator journalkeeperBatchSQLOperator) {
        this.journalkeeperBatchSQLOperator = journalkeeperBatchSQLOperator;
    }

    @Override
    public void insert(String sql, Object... params) {
        journalkeeperBatchSQLOperator.insert(sql, params);
    }

    @Override
    public void update(String sql, Object... params) {
        journalkeeperBatchSQLOperator.update(sql, params);
    }

    @Override
    public void delete(String sql, Object... params) {
        journalkeeperBatchSQLOperator.delete(sql, params);
    }

    @Override
    public List<Object> commit() {
        return journalkeeperBatchSQLOperator.commit();
    }

    @Override
    public void rollback() {
    }
}