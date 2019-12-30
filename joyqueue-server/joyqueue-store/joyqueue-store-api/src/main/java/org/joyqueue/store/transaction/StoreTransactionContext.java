package org.joyqueue.store.transaction;

import java.util.Map;

/**
 * @author LiYue
 * Date: 2019/12/2
 */
public interface StoreTransactionContext {
    StoreTransactionId transactionId();
    Map<String, String> context();
    long timestamp();
}
