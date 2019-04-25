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
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Iterator;
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
    private final List<UnCompletedTransaction> retryUnCompletedTransactionList = Lists.newLinkedList();
    private Map<Long, UnCompletedTransaction> unCompletedTransactionSortedMap;
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
        this.unCompletedTransactionSortedMap = Maps.newLinkedHashMapWithExpectedSize(config.getTransactionLogScanSize());
    }

    public void handle() {
        try {
            handleRetryTransactions();
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
                break;
            }

            lastTime = unCompletedTransaction.getCreateTime();
            unCompletedTransactionSortedMap.put(currentIndex, unCompletedTransaction);
            result.add(transactionDomain);
        }
        return result;
    }

    protected UnCompletedTransaction handlePrepare(TransactionPrepare transactionPrepare, long index) {
        String key = generateKey(transactionPrepare.getApp(), transactionPrepare.getTransactionId(), transactionPrepare.getProducerId(), transactionPrepare.getProducerEpoch());
        UnCompletedTransaction unCompletedTransaction = unCompletedTransactionMap.get(key);
        if (unCompletedTransaction == null) {
            unCompletedTransaction = new UnCompletedTransaction();
            unCompletedTransaction.setStartIndex(index);
            unCompletedTransaction.setId(transactionPrepare.getTransactionId());
            unCompletedTransaction.setApp(transactionPrepare.getApp());
            unCompletedTransaction.setProducerId(transactionPrepare.getProducerId());
            unCompletedTransaction.setProducerEpoch(transactionPrepare.getProducerEpoch());
            unCompletedTransaction.setTimeout(transactionPrepare.getTimeout());
            unCompletedTransaction.setCreateTime(transactionPrepare.getCreateTime());
            unCompletedTransactionMap.put(key, unCompletedTransaction);
        }
        if (unCompletedTransaction.isCompleted() || unCompletedTransaction.isExpired(lastTime, unCompletedTransaction.getCreateTime())) {
            return null;
        }

        unCompletedTransaction.setLastTime(transactionPrepare.getCreateTime());
        unCompletedTransaction.setEndIndex(index);
        unCompletedTransaction.addPrepare(transactionPrepare);
        unCompletedTransaction.setState(TransactionState.ONGOING);
        return unCompletedTransaction;
    }

    protected UnCompletedTransaction handleOffset(TransactionOffset transactionOffset, long index) {
        String key = generateKey(transactionOffset.getApp(), transactionOffset.getTransactionId(), transactionOffset.getProducerId(), transactionOffset.getProducerEpoch());
        UnCompletedTransaction unCompletedTransaction = unCompletedTransactionMap.get(key);
        if (unCompletedTransaction == null) {
            return null;
        }
        if (unCompletedTransaction.isCompleted() || unCompletedTransaction.isExpired(lastTime, unCompletedTransaction.getCreateTime())) {
            return null;
        }

        unCompletedTransaction.setEndIndex(index);
        unCompletedTransaction.addOffset(transactionOffset);
        unCompletedTransaction.setLastTime(transactionOffset.getCreateTime());
        return unCompletedTransaction;
    }

    protected UnCompletedTransaction handleMarker(TransactionMarker transactionMarker, long index) {
        String key = generateKey(transactionMarker.getApp(), transactionMarker.getTransactionId(), transactionMarker.getProducerId(), transactionMarker.getProducerEpoch());
        UnCompletedTransaction unCompletedTransaction = unCompletedTransactionMap.get(key);
        if (unCompletedTransaction == null) {
            return null;
        }
        if (unCompletedTransaction.isCompleted() || unCompletedTransaction.isExpired(lastTime, unCompletedTransaction.getCreateTime())) {
            return null;
        }

        unCompletedTransaction.setEndIndex(index);
        unCompletedTransaction.setState(transactionMarker.getState());
        unCompletedTransaction.setLastTime(transactionMarker.getCreateTime());
        return unCompletedTransaction;
    }

    protected void handleUnCompleteTransactions() throws Exception {
        List<TransactionDomain> transactionDomains = readTransactions();
        if (CollectionUtils.isEmpty(transactionDomains)) {
            return;
        }
        transactionDomains = prepareTransactionDomains(transactionDomains);
        handleUnCompleteTransactions(transactionDomains);
    }

    protected void handleUnCompleteTransactions(List<TransactionDomain> transactionDomains) {
        for (int i = 0; i < transactionDomains.size(); i++) {
            long currentIndex = this.currentIndex + i;
            UnCompletedTransaction unCompletedTransaction = unCompletedTransactionSortedMap.get(currentIndex);
            if (unCompletedTransaction == null) {
                continue;
            }
            if (unCompletedTransaction.isExpired(lastTime, unCompletedTransaction.getTimeout())) {
                // prepare补偿
                if (unCompletedTransaction.isPrepared()) {
                    retryUnCompletedTransactionList.add(unCompletedTransaction);
                } else {
                    // 跳过超时
                    logger.info("事务超时, txId: {}, index: {}", unCompletedTransaction.getId(), currentIndex);
                    unCompletedTransaction.transitionStateTo(TransactionState.DEAD);
                }
            }
            logger.info("currentIndex: {}, isCompleted: {}, epoch: {}, state: {}", currentIndex, unCompletedTransaction.isCompleted(), unCompletedTransaction.getProducerEpoch(), unCompletedTransaction.getState());
        }

        currentIndex += transactionDomains.size();
        logger.info("右移到 {}", currentIndex);
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

            if (currentIndex == unCompletedTransaction.getEndIndex()) {
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
                }
            }
        }

        if (commitIndex < 0) {
            return;
        }

        try {
            transactionLogSegment.saveIndex(commitIndex);
        } catch (Exception e) {
            logger.error("commit index exception", e);
        }

        logger.info("提交index {}", commitIndex);
        logger.info("准备移除, commitIndex: {}, committedIndex: {}", commitIndex, committedIndex);

        for (long i = committedIndex; i < commitIndex; i++) {
            logger.info("移除index {}", i);
            UnCompletedTransaction unCompletedTransaction = unCompletedTransactionSortedMap.remove(i);
            if (unCompletedTransaction != null) {
                logger.info("移除txId {}", generateKey(unCompletedTransaction.getApp(), unCompletedTransaction.getId(),
                        unCompletedTransaction.getProducerId(), unCompletedTransaction.getProducerEpoch()));

                unCompletedTransactionMap.remove(generateKey(unCompletedTransaction.getApp(), unCompletedTransaction.getId(),
                        unCompletedTransaction.getProducerId(), unCompletedTransaction.getProducerEpoch()));
            }
        }

        logger.info("完成移除, sortedMapSize: {}, mapSize: {}", unCompletedTransactionSortedMap.size(), unCompletedTransactionMap.size());

        this.committedIndex = commitIndex;

        logger.info("提交index {}", commitIndex);
    }

    protected void handleRetryTransactions() {
        Iterator<UnCompletedTransaction> iterator = retryUnCompletedTransactionList.iterator();
        while (iterator.hasNext()) {
            UnCompletedTransaction unCompletedTransaction = iterator.next();
            if (unCompletedTransaction.getState().equals(TransactionState.PREPARE_ABORT)) {
                if (tryAbort(unCompletedTransaction)) {
                    unCompletedTransaction.transitionStateTo(TransactionState.COMPLETE_ABORT);
                } else {
                    unCompletedTransaction.incrReties();
                }
            } else if (unCompletedTransaction.getState().equals(TransactionState.PREPARE_COMMIT)) {
                if (tryCommit(unCompletedTransaction)) {
                    unCompletedTransaction.transitionStateTo(TransactionState.COMPLETE_COMMIT);
                } else {
                    unCompletedTransaction.incrReties();
                }
            }

            if (unCompletedTransaction.isCompleted()) {
                logger.info("transaction retry success, metadata: {}", unCompletedTransaction);
                iterator.remove();
            } else {
                if (unCompletedTransaction.getReties() == config.getTransactionLogRetries()) {
                    logger.warn("transaction retry failed, metadata: {}", unCompletedTransaction);
                    unCompletedTransaction.transitionStateTo(TransactionState.DEAD);
                    iterator.remove();
                }
            }
        }
    }

    protected boolean tryAbort(UnCompletedTransaction unCompletedTransaction) {
        try {
            return transactionSynchronizer.abort(unCompletedTransaction, unCompletedTransaction.getPrepare());
        } catch (Exception e) {
            logger.error("tryAbort exception, metadata: {}", unCompletedTransaction);
            return true;
        }
    }

    protected boolean tryCommit(UnCompletedTransaction unCompletedTransaction) {
        try {
            return transactionSynchronizer.commit(unCompletedTransaction, unCompletedTransaction.getPrepare(), unCompletedTransaction.getOffsets());
        } catch (Exception e) {
            logger.error("tryCommit exception, metadata: {}", unCompletedTransaction);
            return false;
        }
    }

    protected String generateKey(String app, String transactionId, long producerId, short producerEpoch) {
        return String.format("%s_%s_%s_%s", app, transactionId, producerId, producerEpoch);
    }
}