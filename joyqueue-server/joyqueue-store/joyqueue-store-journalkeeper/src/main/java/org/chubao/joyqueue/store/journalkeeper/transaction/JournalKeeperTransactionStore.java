package org.chubao.joyqueue.store.journalkeeper.transaction;

import io.journalkeeper.core.api.UpdateRequest;
import io.journalkeeper.core.api.transaction.TransactionalJournalStore;
import io.journalkeeper.exceptions.NotLeaderException;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.store.WriteRequest;
import org.joyqueue.store.WriteResult;
import org.joyqueue.store.transaction.StoreTransactionContext;
import org.joyqueue.store.transaction.StoreTransactionId;
import org.joyqueue.store.transaction.TransactionStore;
import org.joyqueue.toolkit.concurrent.EventListener;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * @author LiYue
 * Date: 2019/12/3
 */
public class JournalKeeperTransactionStore implements TransactionStore {
    private final TransactionalJournalStore transactionalJournalStore;

    public JournalKeeperTransactionStore(TransactionalJournalStore transactionalJournalStore) {
        this.transactionalJournalStore = transactionalJournalStore;
    }

    @Override
    public StoreTransactionContext createTransaction(Map<String, String> context) throws ExecutionException, InterruptedException {
        return transactionalJournalStore.createTransaction(context)
                .thenApply(JournalKeeperTransactionContext::new)
                .get();
    }

    @Override
    public Collection<StoreTransactionContext> getOpeningTransactions() throws ExecutionException, InterruptedException {
        try {
            return transactionalJournalStore.getOpeningTransactions()
                    .thenApply(contexts -> contexts.stream().map(JournalKeeperTransactionContext::new)
                            .map(c -> (StoreTransactionContext) c)
                            .collect(Collectors.toList()))
                    .get();
        } catch (NotLeaderException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public void completeTransaction(StoreTransactionId transactionId, boolean commitOrAbort) throws ExecutionException, InterruptedException {
        transactionalJournalStore.completeTransaction(((JournalKeeperTransactionId) transactionId).getTransactionId(), commitOrAbort).get();
    }

    @Override
    public void asyncWrite(StoreTransactionId transactionId, EventListener<WriteResult> eventListener, WriteRequest... writeRequests) {
        CompletableFuture<WriteResult> future = asyncWrite(transactionId, writeRequests);
        if(null != eventListener) {
            future.thenAccept(eventListener::onEvent);
        }
    }

    @Override
    public CompletableFuture<WriteResult> asyncWrite(StoreTransactionId transactionId, WriteRequest... writeRequests) {
        List<UpdateRequest> updateRequests = Arrays.stream(writeRequests)
                .map(r -> new UpdateRequest(r.getBuffer().array(), r.getPartition(), r.getBatchSize()))
                .collect(Collectors.toList());
        return transactionalJournalStore.append(
                ((JournalKeeperTransactionId) transactionId).getTransactionId(),
                updateRequests, true)
                .thenApply(aVoid -> new WriteResult(JoyQueueCode.SUCCESS, new long[0]))
                .exceptionally(e -> new WriteResult(JoyQueueCode.SE_WRITE_FAILED, new long [0])); // TODO: 精细化返回值，针对各种错误情况做映射
    }
}
