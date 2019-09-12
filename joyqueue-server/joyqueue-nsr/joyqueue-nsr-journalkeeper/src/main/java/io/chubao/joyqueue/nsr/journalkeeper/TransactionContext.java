package io.chubao.joyqueue.nsr.journalkeeper;

import io.journalkeeper.sql.client.SQLOperator;
import io.journalkeeper.sql.client.SQLTransactionOperator;

/**
 * TransactionContext
 * author: gaohaoxiang
 * date: 2019/8/15
 */
public class TransactionContext {

    private static final ThreadLocal<SQLTransactionOperator> transactionThreadLocal = new ThreadLocal<>();

    private static SQLOperator sqlOperator;

    public static void init(SQLOperator sqlOperator) {
        if (TransactionContext.sqlOperator != null) {
            return;
        }
        TransactionContext.sqlOperator = sqlOperator;
    }

    public static void beginTransaction() {
        if (transactionThreadLocal.get() != null) {
            return;
        }
        SQLTransactionOperator transactionOperator = sqlOperator.beginTransaction();
        transactionThreadLocal.set(transactionOperator);
    }

    public static SQLTransactionOperator getTransactionOperator() {
        return transactionThreadLocal.get();
    }

    public static void commit() {
        SQLTransactionOperator transactionOperator = transactionThreadLocal.get();
        if (transactionOperator == null) {
            throw new UnsupportedOperationException("transaction not exist");
        }
        try {
            transactionOperator.commit();
        } finally {
            transactionThreadLocal.remove();
        }
    }

    public static void rollback() {
        SQLTransactionOperator transactionOperator = transactionThreadLocal.get();
        if (transactionOperator == null) {
            throw new UnsupportedOperationException("transaction not exist");
        }
        try {
            transactionOperator.rollback();
        } finally {
            transactionThreadLocal.remove();
        }
    }

    public static void close() {
        SQLTransactionOperator transactionOperator = transactionThreadLocal.get();
        if (transactionOperator == null) {
            throw new UnsupportedOperationException("transaction not exist");
        }
        transactionThreadLocal.remove();
    }
}