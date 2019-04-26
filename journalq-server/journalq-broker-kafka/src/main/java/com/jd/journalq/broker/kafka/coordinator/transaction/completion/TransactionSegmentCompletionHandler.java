package com.jd.journalq.broker.kafka.coordinator.transaction.completion;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jd.journalq.broker.kafka.config.KafkaConfig;
import com.jd.journalq.broker.kafka.coordinator.Coordinator;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionDomain;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionMarker;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionOffset;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionPrepare;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionState;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.UnCompletedTransaction;
import com.jd.journalq.broker.kafka.coordinator.transaction.log.TransactionLogSegment;
import com.jd.journalq.broker.kafka.coordinator.transaction.synchronizer.TransactionSynchronizer;
import com.jd.journalq.toolkit.time.SystemClock;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * TransactionCompletionHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/19
 */
public class TransactionSegmentCompletionHandler {

    protected static final Logger logger = LoggerFactory.getLogger(TransactionSegmentCompletionHandler.class);

    private KafkaConfig config;
    private Coordinator coordinator;
    private TransactionLogSegment transactionLogSegment;
    private TransactionSynchronizer transactionSynchronizer;

    private final Map<String, UnCompletedTransaction> unCompletedTransactionMap = Maps.newLinkedHashMap();
    private final Map<Long, UnCompletedTransaction> unCompletedTransactionSortedMap = Maps.newLinkedHashMap();
    private long lastTime = 0;
    private long startIndex = 0;
    private long currentIndex = 0;
    private long committedIndex = 0;

    public TransactionSegmentCompletionHandler(KafkaConfig config, Coordinator coordinator, TransactionLogSegment transactionLogSegment, TransactionSynchronizer transactionSynchronizer) {
        this.config = config;
        this.coordinator = coordinator;
        this.transactionLogSegment = transactionLogSegment;
        this.transactionSynchronizer = transactionSynchronizer;
        this.currentIndex = transactionLogSegment.getIndex();
        this.startIndex = currentIndex;
        this.committedIndex = currentIndex;
    }

    public void handle() {
        try {
            handleUnCompleteTransactions();
            commitUnCompleteTransactionIndex();
        } catch (Exception e) {
            logger.error("transaction compensate exception", e);
        }
    }

    protected List<TransactionDomain> readTransactions() throws Exception {
        List<TransactionDomain> transactionDomains = transactionLogSegment.read(currentIndex, config.getTransactionLogScanSize());
        if (CollectionUtils.isEmpty(transactionDomains)) {
            return Collections.emptyList();
        }
        return transactionDomains;
    }

    protected List<TransactionDomain> prepareTransactionDomains(List<TransactionDomain> transactionDomains) {
        List<TransactionDomain> result = Lists.newLinkedList();
        for (int i = 0; i < transactionDomains.size(); i++) {
            long currentIndex = this.currentIndex + i;
            TransactionDomain transactionDomain = transactionDomains.get(i);
            UnCompletedTransaction unCompletedTransaction = null;

            if (transactionDomain instanceof TransactionPrepare) {
                unCompletedTransaction = handlePrepare((TransactionPrepare) transactionDomain, currentIndex);
            } else if (transactionDomain instanceof TransactionOffset) {
                unCompletedTransaction = handleOffset((TransactionOffset) transactionDomain, currentIndex);
            } else if (transactionDomain instanceof TransactionMarker) {
                unCompletedTransaction = handleMarker((TransactionMarker) transactionDomain, currentIndex);
            } else {
                logger.warn("unsupported transaction domain, type: {}", transactionDomain);
            }
            if (unCompletedTransaction == null) {
                result.add(null);
            } else {
                lastTime = unCompletedTransaction.getCreateTime();
                unCompletedTransactionSortedMap.put(currentIndex, unCompletedTransaction);
                result.add(transactionDomain);
            }
        }
        return result;
    }

    protected UnCompletedTransaction handlePrepare(TransactionPrepare transactionPrepare, long index) {
        String key = generateKey(transactionPrepare.getApp(), transactionPrepare.getTransactionId(), transactionPrepare.getProducerId(), transactionPrepare.getProducerEpoch(), transactionPrepare.getEpoch());
        UnCompletedTransaction unCompletedTransaction = unCompletedTransactionMap.get(key);
        if (unCompletedTransaction == null) {
            unCompletedTransaction = new UnCompletedTransaction();
            unCompletedTransaction.setId(transactionPrepare.getTransactionId());
            unCompletedTransaction.setStartIndex(index);
            unCompletedTransaction.setApp(transactionPrepare.getApp());
            unCompletedTransaction.setProducerId(transactionPrepare.getProducerId());
            unCompletedTransaction.setProducerEpoch(transactionPrepare.getProducerEpoch());
            unCompletedTransaction.setEpoch(transactionPrepare.getEpoch());
            unCompletedTransaction.setTimeout(transactionPrepare.getTimeout());
            unCompletedTransaction.setCreateTime(transactionPrepare.getCreateTime());
            unCompletedTransactionMap.put(key, unCompletedTransaction);
        }
        unCompletedTransaction.setLastTime(transactionPrepare.getCreateTime());
        unCompletedTransaction.setEndIndex(index);
        unCompletedTransaction.addPrepare(transactionPrepare);
        unCompletedTransaction.setState(TransactionState.ONGOING);
        return unCompletedTransaction;
    }

    protected UnCompletedTransaction handleOffset(TransactionOffset transactionOffset, long index) {
        String key = generateKey(transactionOffset.getApp(), transactionOffset.getTransactionId(), transactionOffset.getProducerId(), transactionOffset.getProducerEpoch(), transactionOffset.getEpoch());
        UnCompletedTransaction unCompletedTransaction = unCompletedTransactionMap.get(key);
        if (unCompletedTransaction == null) {
            return null;
        }
        unCompletedTransaction.setEndIndex(index);
        unCompletedTransaction.addOffset(transactionOffset);
        unCompletedTransaction.setLastTime(transactionOffset.getCreateTime());
        return unCompletedTransaction;
    }

    protected UnCompletedTransaction handleMarker(TransactionMarker transactionMarker, long index) {
        String key = generateKey(transactionMarker.getApp(), transactionMarker.getTransactionId(), transactionMarker.getProducerId(), transactionMarker.getProducerEpoch(), transactionMarker.getEpoch());
        UnCompletedTransaction unCompletedTransaction = unCompletedTransactionMap.get(key);
        if (unCompletedTransaction == null) {
            return null;
        }
        unCompletedTransaction.setEndIndex(index);
        unCompletedTransaction.setState(transactionMarker.getState());
        unCompletedTransaction.setLastTime(transactionMarker.getCreateTime());
        return unCompletedTransaction;
    }

    protected void handleUnCompleteTransactions() throws Exception {
        List<TransactionDomain> transactionDomains = readTransactions();
        if (CollectionUtils.isNotEmpty(transactionDomains)) {
            transactionDomains = prepareTransactionDomains(transactionDomains);
        }
        handleUnCompleteTransactions(transactionDomains);
    }

    protected void handleUnCompleteTransactions(List<TransactionDomain> transactionDomains) {
        long lastTime = Math.max(this.lastTime, SystemClock.now());
        for (Map.Entry<Long, UnCompletedTransaction> entry : unCompletedTransactionSortedMap.entrySet()) {
            long currentIndex = entry.getKey();
            UnCompletedTransaction unCompletedTransaction = entry.getValue();
            if (unCompletedTransaction != null) {
                logger.debug("read transaction, currentIndex: {}, isCompleted: {}, epoch: {}, state: {}, metadata: {}",
                        currentIndex, unCompletedTransaction.isCompleted(), unCompletedTransaction.getProducerEpoch(), unCompletedTransaction.getState(), unCompletedTransaction);
            }
            if (unCompletedTransaction == null || unCompletedTransaction.isCompleted()) {
                continue;
            }
            if (unCompletedTransaction.isExpired(lastTime, unCompletedTransaction.getTimeout())) {
                if (unCompletedTransaction.isPrepared()) {
                    handleRetryTransaction(unCompletedTransaction);
                } else {
                    handleTimeoutTransaction(unCompletedTransaction);
                }
            }
        }

        if (CollectionUtils.isNotEmpty(transactionDomains)) {
            currentIndex += transactionDomains.size();
            logger.debug("left transaction index: {}", currentIndex);
        }
    }

    protected void commitUnCompleteTransactionIndex() {
        long commitIndex = -1;

        for (Map.Entry<Long, UnCompletedTransaction> entry : unCompletedTransactionSortedMap.entrySet()) {
            long currentIndex = entry.getKey();
            UnCompletedTransaction unCompletedTransaction = entry.getValue();
            if (unCompletedTransaction == null) {
                continue;
            }
            if (!unCompletedTransaction.isCompleted()) {
                break;
            }
            if (currentIndex != unCompletedTransaction.getEndIndex()) {
                continue;
            }

            boolean isCommit = true;
            for (long j = Math.max((int) commitIndex, committedIndex); j < unCompletedTransaction.getEndIndex(); j++) {
                UnCompletedTransaction checkUnCompletedTransaction = unCompletedTransactionSortedMap.get(j);
                if (checkUnCompletedTransaction == null) {
                    continue;
                }
                if (!checkUnCompletedTransaction.isCompleted()) {
                    isCommit = false;
                    break;
                }
            }

            if (isCommit) {
                commitIndex = currentIndex + 1;
            } else {
                break;
            }
        }

        if (commitIndex < 0) {
            return;
        }

        try {
            logger.info("commit transaction index {}", commitIndex);
            transactionLogSegment.saveIndex(commitIndex);
        } catch (Exception e) {
            logger.error("commit transaction index exception", e);
        }

        logger.debug("remove transaction cache, commitIndex: {}, committedIndex: {}", commitIndex, committedIndex);

        for (long i = committedIndex; i < commitIndex; i++) {
            logger.debug("remove transaction cache {}", i);
            UnCompletedTransaction unCompletedTransaction = unCompletedTransactionSortedMap.remove(i);
            if (unCompletedTransaction != null) {
                logger.debug("remove transaction cache, txId: {}", generateKey(unCompletedTransaction.getApp(), unCompletedTransaction.getId(),
                        unCompletedTransaction.getProducerId(), unCompletedTransaction.getProducerEpoch(), unCompletedTransaction.getEpoch()));

                unCompletedTransactionMap.remove(generateKey(unCompletedTransaction.getApp(), unCompletedTransaction.getId(),
                        unCompletedTransaction.getProducerId(), unCompletedTransaction.getProducerEpoch(), unCompletedTransaction.getEpoch()));
            }
        }

        logger.debug("remove transaction cache complete, sortedMapSize: {}, mapSize: {}", unCompletedTransactionSortedMap.size(), unCompletedTransactionMap.size());

        this.committedIndex = commitIndex;
    }

    protected void handleRetryTransaction(UnCompletedTransaction unCompletedTransaction) {
        logger.debug("retry transaction, txId: {}, metadata: {}", unCompletedTransaction.getId(), unCompletedTransaction);

        if (unCompletedTransaction.getState().equals(TransactionState.PREPARE_COMMIT)) {
            if (tryCommit(unCompletedTransaction)) {
                unCompletedTransaction.transitionStateTo(TransactionState.COMPLETE_COMMIT);
            } else {
                unCompletedTransaction.incrReties();
            }
        } else if (unCompletedTransaction.getState().equals(TransactionState.PREPARE_ABORT)) {
            if (tryAbort(unCompletedTransaction)) {
                unCompletedTransaction.transitionStateTo(TransactionState.COMPLETE_ABORT);
            } else {
                unCompletedTransaction.incrReties();
            }
        }

        if (unCompletedTransaction.isCompleted()) {
            logger.info("retry transaction success, metadata: {}", unCompletedTransaction);
        } else {
            if (unCompletedTransaction.getReties() >= config.getTransactionLogRetries()) {
                logger.warn("retry transaction failed, metadata: {}", unCompletedTransaction);
                unCompletedTransaction.transitionStateTo(TransactionState.DEAD);
            }
        }
    }

    protected void handleTimeoutTransaction(UnCompletedTransaction unCompletedTransaction) {
        logger.warn("transaction timeout, txId: {}, metadata: {}", unCompletedTransaction.getId(), unCompletedTransaction);
        unCompletedTransaction.transitionStateTo(TransactionState.DEAD);
    }

    protected boolean tryAbort(UnCompletedTransaction unCompletedTransaction) {
        try {
            return transactionSynchronizer.tryAbort(unCompletedTransaction, unCompletedTransaction.getPrepare());
        } catch (Exception e) {
            logger.error("tryAbort transaction exception, metadata: {}", unCompletedTransaction);
            return true;
        }
    }

    protected boolean tryCommit(UnCompletedTransaction unCompletedTransaction) {
        try {
            return transactionSynchronizer.tryCommit(unCompletedTransaction, unCompletedTransaction.getPrepare(), unCompletedTransaction.getOffsets());
        } catch (Exception e) {
            logger.error("tryCommit transaction exception, metadata: {}", unCompletedTransaction);
            return false;
        }
    }

    protected String generateKey(String app, String transactionId, long producerId, short producerEpoch, short epoch) {
        return String.format("%s_%s_%s_%s_%s", app, transactionId, producerId, producerEpoch, epoch);
    }
}