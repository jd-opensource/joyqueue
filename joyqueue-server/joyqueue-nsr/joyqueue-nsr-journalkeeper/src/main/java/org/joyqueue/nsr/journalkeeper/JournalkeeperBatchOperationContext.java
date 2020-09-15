package org.joyqueue.nsr.journalkeeper;

import org.joyqueue.nsr.sql.operator.BatchSQLOperator;
import org.joyqueue.nsr.sql.operator.SQLOperator;

/**
 * JournalkeeperBatchOperationContext
 * author: gaohaoxiang
 * date: 2019/8/15
 */
public class JournalkeeperBatchOperationContext {

    private static final ThreadLocal<BatchSQLOperator> batchOperatorThreadLocal = new ThreadLocal<>();

    private static SQLOperator sqlOperator;

    public static void init(SQLOperator sqlOperator) {
        if (JournalkeeperBatchOperationContext.sqlOperator != null) {
            return;
        }
        JournalkeeperBatchOperationContext.sqlOperator = sqlOperator;
    }

    public static void begin() {
        BatchSQLOperator batchSQLOperator = sqlOperator.beginBatch();
        batchOperatorThreadLocal.set(batchSQLOperator);
    }

    public static void commit() {
        BatchSQLOperator batchSQLOperator = batchOperatorThreadLocal.get();
        if (batchSQLOperator == null) {
            throw new UnsupportedOperationException("batch not exist");
        }
        batchSQLOperator.commit();
        batchOperatorThreadLocal.remove();
    }

    public static void rollback() {
        BatchSQLOperator batchSQLOperator = batchOperatorThreadLocal.get();
        if (batchSQLOperator == null) {
            throw new UnsupportedOperationException("batch not exist");
        }
        batchSQLOperator.rollback();
        batchOperatorThreadLocal.remove();
    }

    public static void close() {
        BatchSQLOperator batchSQLOperator = batchOperatorThreadLocal.get();
        if (batchSQLOperator == null) {
            throw new UnsupportedOperationException("batch not exist");
        }
        batchOperatorThreadLocal.remove();
    }

    public static BatchSQLOperator getBatchSQLOperator() {
        return batchOperatorThreadLocal.get();
    }
}