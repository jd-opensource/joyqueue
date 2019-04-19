package com.jd.journalq.broker.kafka.coordinator.transaction;

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
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * TransactionCompensateThread
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/19
 */
// TODO 协调者变化处理
public class TransactionCompensateThread implements Runnable {

    protected static final Logger logger = LoggerFactory.getLogger(TransactionCompensateThread.class);

    private KafkaConfig config;
    private Coordinator coordinator;
    private TransactionLog transactionLog;
    private TransactionSynchronizer transactionSynchronizer;

    private final Map<String, UnCompletedTransaction> unCompletedTransactionMap = Maps.newLinkedHashMap();
    private long currentIndex = 0;
    private long commitedIndex = 0;

    public TransactionCompensateThread(KafkaConfig config, Coordinator coordinator, TransactionLog transactionLog, TransactionSynchronizer transactionSynchronizer) {
        this.config = config;
        this.coordinator = coordinator;
        this.transactionLog = transactionLog;
        this.transactionSynchronizer = transactionSynchronizer;
        this.currentIndex = transactionLog.getIndex();
        this.commitedIndex = currentIndex;
    }

    @Override
    public void run() {
        try {
            List<TransactionDomain> transactionDomains = transactionLog.read(currentIndex, config.getTransactionLogScanSize());
            if (CollectionUtils.isEmpty(transactionDomains)) {
                return;
            }

            for (int i = 0; i < transactionDomains.size(); i++) {
                long currentIndex = this.currentIndex + i;
                TransactionDomain transactionDomain = transactionDomains.get(i);
                if (transactionDomain instanceof TransactionPrepare) {
                    handlePrepare((TransactionPrepare) transactionDomain, currentIndex);
                } else if (transactionDomain instanceof TransactionOffset) {
                    handleOffset((TransactionOffset) transactionDomain, currentIndex);
                } else if (transactionDomain instanceof TransactionMarker) {
                    handleMarker((TransactionMarker) transactionDomain, currentIndex);
                } else {
                    logger.warn("unsupported transaction domain, type: {}", transactionDomain);
                }
            }

            checkUnCompleteTransactions(transactionDomains);
        } catch (Exception e) {
            logger.error("transaction compensate exception", e);
        }
    }

    protected void handlePrepare(TransactionPrepare transactionPrepare, long index) {
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

        unCompletedTransaction.setEndIndex(index);
        unCompletedTransaction.addPrepare(transactionPrepare);
        unCompletedTransaction.setState(TransactionState.ONGOING);
    }

    protected void handleOffset(TransactionOffset transactionOffset, long index) {
        String key = generateKey(transactionOffset.getApp(), transactionOffset.getTransactionId(), transactionOffset.getProducerId(), transactionOffset.getProducerEpoch());
        UnCompletedTransaction unCompletedTransaction = unCompletedTransactionMap.get(key);
        if (unCompletedTransaction == null) {
            return;
        }

        unCompletedTransaction.setEndIndex(index);
        unCompletedTransaction.addOffset(transactionOffset);
    }

    protected void handleMarker(TransactionMarker transactionMarker, long index) {
        String key = generateKey(transactionMarker.getApp(), transactionMarker.getTransactionId(), transactionMarker.getProducerId(), transactionMarker.getProducerEpoch());
        UnCompletedTransaction unCompletedTransaction = unCompletedTransactionMap.get(key);
        if (unCompletedTransaction == null) {
            return;
        }

        unCompletedTransaction.setEndIndex(index);
        unCompletedTransaction.setState(transactionMarker.getState());
    }

    protected void checkUnCompleteTransactions(List<TransactionDomain> transactionDomains) {
        // 如果已经是终态，后移真正index (COMPLETE，TIMEOUT)，并提交index
        // 如果有未完成的事务 (PREPARE, ONGOING), 后移当前index
        // 优化点，找到非终态但实际已经是终态的事务 (epoch已经变更)
        // 优化点，交叉事务处理

        long commitIndex = -1;

        List<UnCompletedTransaction> unCompletedTransactionList = Lists.newArrayList(unCompletedTransactionMap.values());
        for (int i = 0; i < unCompletedTransactionList.size(); i++) {
            boolean isCompleted = false;
            UnCompletedTransaction unCompletedTransaction = unCompletedTransactionList.get(i);

            if (unCompletedTransaction.getState().equals(TransactionState.COMPLETE_COMMIT) ||
                    unCompletedTransaction.getState().equals(TransactionState.COMPLETE_ABORT)) {
                isCompleted = true;
            } else {

            }

            if (isCompleted) {
                if (commitIndex < 0 && commitIndex == unCompletedTransaction.getStartIndex()) {

                }
            }
        }
    }

    protected String generateKey(String app, String transactionId, long producerId, short producerEpoch) {
        return String.format("%s_%s_%s_%s", app, transactionId, producerId, producerEpoch);
    }
}