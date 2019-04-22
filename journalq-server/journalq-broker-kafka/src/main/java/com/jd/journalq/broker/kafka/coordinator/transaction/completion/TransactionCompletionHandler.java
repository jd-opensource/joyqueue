package com.jd.journalq.broker.kafka.coordinator.transaction.completion;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jd.journalq.broker.kafka.config.KafkaConfig;
import com.jd.journalq.broker.kafka.coordinator.Coordinator;
import com.jd.journalq.broker.kafka.coordinator.transaction.TransactionLog;
import com.jd.journalq.broker.kafka.coordinator.transaction.TransactionSynchronizer;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionDomain;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionMarker;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionOffset;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionPrepare;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionState;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.UnCompletedTransaction;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * TransactionCompletionHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/19
 */
// TODO 临时代码
// TODO 补充日志
// TODO 协调者变化处理
public class TransactionCompletionHandler {

    protected static final Logger logger = LoggerFactory.getLogger(TransactionCompletionHandler.class);

    private KafkaConfig config;
    private Coordinator coordinator;
    private TransactionLog transactionLog;
    private TransactionSynchronizer transactionSynchronizer;

    private final Map<String, UnCompletedTransaction> unCompletedTransactionMap = Maps.newLinkedHashMap();
    private final List<UnCompletedTransaction> unCompletedTransactionList = Lists.newArrayListWithCapacity(config.getTransactionLogScanSize());
    private long lastTime = 0;
    private long startIndex = 0;
    private long currentIndex = 0;
    private long commitedIndex = 0;

    public TransactionCompletionHandler(KafkaConfig config, Coordinator coordinator, TransactionLog transactionLog, TransactionSynchronizer transactionSynchronizer) {
        this.config = config;
        this.coordinator = coordinator;
        this.transactionLog = transactionLog;
        this.transactionSynchronizer = transactionSynchronizer;
        this.currentIndex = transactionLog.getIndex();
        this.startIndex = currentIndex;
        this.commitedIndex = currentIndex;
    }

    public void handle() {
        try {
            List<TransactionDomain> transactionDomains = transactionLog.read(currentIndex, config.getTransactionLogScanSize());
            if (CollectionUtils.isEmpty(transactionDomains)) {
                return;
            }

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

                if (unCompletedTransaction != null) {
                    lastTime = unCompletedTransaction.getCreateTime();
                }
                unCompletedTransactionList.add(unCompletedTransaction);
            }

            checkUnCompleteTransactions(transactionDomains);
        } catch (Exception e) {
            logger.error("transaction compensate exception", e);
        }
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
        if (unCompletedTransaction.isCompleted()) {
            return unCompletedTransaction;
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
        if (unCompletedTransaction.isCompleted()) {
            return unCompletedTransaction;
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
        if (unCompletedTransaction.isCompleted()) {
            return unCompletedTransaction;
        }

        unCompletedTransaction.setEndIndex(index);
        unCompletedTransaction.setState(transactionMarker.getState());
        unCompletedTransaction.setLastTime(transactionMarker.getCreateTime());
        return unCompletedTransaction;
    }

    protected void checkUnCompleteTransactions(List<TransactionDomain> transactionDomains) {
        long commitIndex = -1;
        boolean isNeedRetry = false;

        for (int i = (int) (this.currentIndex - this.startIndex); i < unCompletedTransactionList.size(); i++) {
            long currentIndex = this.startIndex + i;
            UnCompletedTransaction unCompletedTransaction = unCompletedTransactionList.get(i);
            boolean isCompleted = false;

            if (unCompletedTransaction.isCompleted()) {
                // 正常事务提交
                if (currentIndex == unCompletedTransaction.getEndIndex()) {
                    isCompleted = true;
                }
            } else if (unCompletedTransaction.isExpired(lastTime, unCompletedTransaction.getTimeout())) {

                // prepare事务补偿
                if (unCompletedTransaction.isPrepared()) {
                    if (unCompletedTransaction.getState().equals(TransactionState.PREPARE_ABORT)) {
                        if (!tryAbort(unCompletedTransaction)) {
                            isNeedRetry = true;
                            unCompletedTransaction.incrReties();
                        }
                    } else if (unCompletedTransaction.getState().equals(TransactionState.PREPARE_COMMIT)) {
                        if (!tryCommit(unCompletedTransaction)) {
                            isNeedRetry = true;
                            unCompletedTransaction.incrReties();
                        }
                    }

                    if (unCompletedTransaction.getReties() == config.getTransactionLogRetries()) {
                        logger.warn("transaction retry failed, metadata: {}", unCompletedTransaction);
                        unCompletedTransaction.setState(TransactionState.DEAD);
                    }
                } else {
                    // 跳过超时事务
                    continue;
                }
            }

            if (isCompleted && (commitIndex == -1 || commitIndex == commitIndex)) {
                commitIndex = currentIndex + 1;
            }

            logger.info("currentIndex: {}, isCompleted: {}", currentIndex, isCompleted);
        }

        // 如果需要重试，不做处理
        if (isNeedRetry) {
            logger.info("retry");
            return;
        }

        currentIndex += transactionDomains.size();
        logger.info("右移到 {}", currentIndex);

        // 判断index是否右移
        if (commitIndex != -1) {
            boolean isCommit = true;
            for (int i = 0; i < commitIndex - this.startIndex; i++) {
                UnCompletedTransaction unCompletedTransaction = unCompletedTransactionList.get(i);
                if (unCompletedTransaction != null && !unCompletedTransaction.isCompleted()) {
                    isCommit = false;
                    break;
                }
            }

            if (isCommit) {
                try {
                    transactionLog.saveIndex(commitIndex);
                } catch (Exception e) {
                    logger.error("commit index exception", e);
                }

                Iterator<UnCompletedTransaction> iterator = unCompletedTransactionList.iterator();
                for (int i = 0; i < commitIndex - this.startIndex; i++) {
                    UnCompletedTransaction unCompletedTransaction = iterator.next();
                    if (unCompletedTransaction != null) {
                        unCompletedTransactionMap.remove(generateKey(unCompletedTransaction.getApp(), unCompletedTransaction.getId(),
                                unCompletedTransaction.getProducerId(), unCompletedTransaction.getProducerEpoch()));
                    }
                    iterator.remove();
                }

                this.startIndex = commitIndex;
                this.commitedIndex = commitIndex;
                logger.info("提交index {}", commitIndex);
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