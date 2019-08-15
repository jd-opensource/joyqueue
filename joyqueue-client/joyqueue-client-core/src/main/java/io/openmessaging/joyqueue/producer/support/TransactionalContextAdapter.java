package io.openmessaging.joyqueue.producer.support;

import io.chubao.joyqueue.client.internal.producer.domain.TransactionStatus;
import io.openmessaging.producer.TransactionStateCheckListener;

/**
 * TransactionalContextAdapter
 *
 * author: gaohaoxiang
 * date: 2019/2/19
 */
public class TransactionalContextAdapter implements TransactionStateCheckListener.TransactionalContext {

    private TransactionStatus status;

    @Override
    public void commit() {
        status = TransactionStatus.PREPARE;
    }

    @Override
    public void rollback() {
        status = TransactionStatus.ROLLBACK;
    }

    @Override
    public void unknown() {
        status = TransactionStatus.UNKNOWN;
    }

    public TransactionStatus getStatus() {
        return status;
    }
}