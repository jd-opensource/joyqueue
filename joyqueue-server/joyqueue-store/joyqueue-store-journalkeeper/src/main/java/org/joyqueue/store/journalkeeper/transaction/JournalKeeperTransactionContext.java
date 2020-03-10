package org.joyqueue.store.journalkeeper.transaction;

import io.journalkeeper.core.api.transaction.TransactionContext;
import org.joyqueue.store.transaction.StoreTransactionContext;
import org.joyqueue.store.transaction.StoreTransactionId;

import java.util.Map;

/**
 * @author LiYue
 * Date: 2019/12/3
 */
public class JournalKeeperTransactionContext implements StoreTransactionContext {
    private final TransactionContext journalkeeperTransactionContext;

    public JournalKeeperTransactionContext(TransactionContext journalkeeperTransactionContext) {
        this.journalkeeperTransactionContext = journalkeeperTransactionContext;
    }

    @Override
    public StoreTransactionId transactionId() {
        return new JournalKeeperTransactionId(journalkeeperTransactionContext.transactionId());
    }

    @Override
    public Map<String, String> context() {
        return journalkeeperTransactionContext.context();
    }

    @Override
    public long timestamp() {
        return journalkeeperTransactionContext.timestamp();
    }
}
