package org.joyqueue.store.journalkeeper.transaction;

import io.journalkeeper.core.api.transaction.TransactionId;
import org.joyqueue.store.transaction.StoreTransactionId;

/**
 * @author LiYue
 * Date: 2019/12/3
 */
public class JournalKeeperTransactionId implements StoreTransactionId {
    private final TransactionId transactionId;

    public JournalKeeperTransactionId(TransactionId transactionId) {
        this.transactionId = transactionId;
    }

    public TransactionId getTransactionId() {
        return transactionId;
    }

    @Override
    public String toString() {
        return transactionId.toString();
    }
}
