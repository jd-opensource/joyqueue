package org.joyqueue.nsr.sql;

import org.joyqueue.nsr.sql.operator.BatchSQLOperator;

import java.util.List;

/**
 * CompositionBatchSQLOperator
 * author: gaohaoxiang
 * date: 2020/8/13
 */
public class CompositionBatchSQLOperator implements BatchSQLOperator {

    private List<BatchSQLOperator> batchSQLOperators;

    public CompositionBatchSQLOperator(List<BatchSQLOperator> batchSQLOperators) {
        this.batchSQLOperators = batchSQLOperators;
    }

    @Override
    public void insert(String sql, Object... params) {
        for (BatchSQLOperator batchSQLOperator : batchSQLOperators) {
            batchSQLOperator.insert(sql, params);
        }
    }

    @Override
    public void update(String sql, Object... params) {
        for (BatchSQLOperator batchSQLOperator : batchSQLOperators) {
            batchSQLOperator.update(sql, params);
        }
    }

    @Override
    public void delete(String sql, Object... params) {
        for (BatchSQLOperator batchSQLOperator : batchSQLOperators) {
            batchSQLOperator.delete(sql, params);
        }
    }

    @Override
    public List<Object> commit() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void rollback() {
        throw new UnsupportedOperationException();
    }
}