package io.chubao.joyqueue.nsr.journalkeeper;

import io.journalkeeper.sql.client.BatchSQLOperator;
import io.journalkeeper.sql.client.SQLOperator;

/**
 * BatchOperationContext
 * author: gaohaoxiang
 * date: 2019/8/15
 */
public class BatchOperationContext {

    private static final ThreadLocal<BatchSQLOperator> batchOperatorThreadLocal = new ThreadLocal<>();

    private static SQLOperator sqlOperator;

    public static void init(SQLOperator sqlOperator) {
        if (BatchOperationContext.sqlOperator != null) {
            return;
        }
        BatchOperationContext.sqlOperator = sqlOperator;
    }

    public static void begin() {
        if (batchOperatorThreadLocal.get() != null) {
            throw new UnsupportedOperationException("batch is exist");
        }
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