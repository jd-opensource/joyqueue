package io.chubao.joyqueue.nsr.ignite;

import io.chubao.joyqueue.nsr.service.internal.TransactionInternalService;
import org.apache.ignite.Ignition;
import org.apache.ignite.transactions.Transaction;
import org.apache.ignite.transactions.TransactionConcurrency;
import org.apache.ignite.transactions.TransactionIsolation;

/**
 * IgniteTransactionInternalService
 * author: gaohaoxiang
 * date: 2019/8/28
 */
public class IgniteTransactionInternalService implements TransactionInternalService {

    private final ThreadLocal<Transaction> transactionThreadLocal = new ThreadLocal<>();

    @Override
    public void begin() {
        if (transactionThreadLocal.get() != null) {
            throw new UnsupportedOperationException("transport is exist");
        }
        // TODO 临时处理超时时间
        Transaction transaction = Ignition.ignite().transactions().txStart(TransactionConcurrency.PESSIMISTIC, TransactionIsolation.READ_COMMITTED, 1000 * 60, 0);
        transactionThreadLocal.set(transaction);
    }

    @Override
    public void commit() {
        Transaction transaction = transactionThreadLocal.get();
        if (transaction == null) {
            throw new UnsupportedOperationException("transaction not exist");
        }
        try {
            transaction.commit();
        } finally {
            transactionThreadLocal.remove();
        }
    }

    @Override
    public void rollback() {
        Transaction transaction = transactionThreadLocal.get();
        if (transaction == null) {
            throw new UnsupportedOperationException("transaction not exist");
        }
        try {
            transaction.rollback();
        } finally {
            transactionThreadLocal.remove();
        }
    }
}