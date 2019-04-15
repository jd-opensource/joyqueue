package com.jd.journalq.broker.kafka.coordinator.transaction;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.jd.journalq.broker.coordinator.session.CoordinatorSession;
import com.jd.journalq.broker.coordinator.session.CoordinatorSessionManager;
import com.jd.journalq.broker.kafka.config.KafkaConfig;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionMarker;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionPrepare;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionState;
import com.jd.journalq.broker.producer.transaction.command.TransactionCommitRequest;
import com.jd.journalq.broker.producer.transaction.command.TransactionRollbackRequest;
import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.CommandCallback;
import com.jd.journalq.network.transport.command.JMQCommand;
import com.jd.journalq.toolkit.service.Service;
import com.jd.journalq.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * TransactionSynchronizer
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/12
 */
// TODO 定期同步事务状态
// TODO 补充日志
// TODO 事务日志写入失败处理
public class TransactionSynchronizer extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(TransactionSynchronizer.class);

    private KafkaConfig config;
    private TransactionIdManager transactionIdManager;
    private TransactionLog transactionLog;
    private CoordinatorSessionManager sessionManager;

    public TransactionSynchronizer(KafkaConfig config, TransactionIdManager transactionIdManager, TransactionLog transactionLog, CoordinatorSessionManager sessionManager) {
        this.config = config;
        this.transactionIdManager = transactionIdManager;
        this.transactionLog = transactionLog;
        this.sessionManager = sessionManager;
    }

    public boolean prepare(TransactionPrepare prepare) throws Exception {
        return transactionLog.writePrepare(prepare);
    }

    public boolean prepareCommit(List<TransactionPrepare> prepareList) throws Exception {
        return writeMarker(prepareList.get(0), TransactionState.PREPARE_COMMIT);
    }

    public boolean commit(List<TransactionPrepare> prepareList) throws Exception {
        prepareList = filterPrepareByBroker(prepareList);
        CountDownLatch latch = new CountDownLatch(prepareList.size());
        boolean[] result = {true};

        for (TransactionPrepare prepare : prepareList) {
            CoordinatorSession session = sessionManager.getOrCreateSession(prepare.getBrokerId(), prepare.getBrokerHost(), prepare.getBrokerPort());
            String txId = transactionIdManager.generateId(prepare.getTopic(), prepare.getApp(), prepare.getTransactionId(), prepare.getProducerId(), prepare.getProducerEpoch());
            TransactionCommitRequest transactionCommitRequest = new TransactionCommitRequest(prepare.getTopic(), prepare.getApp(), txId);
            session.async(new JMQCommand(transactionCommitRequest), new CommandCallback() {
                @Override
                public void onSuccess(Command request, Command response) {
                    if (response.getHeader().getStatus() != JMQCode.CN_TRANSACTION_NOT_EXISTS.getCode()) {
                        result[0] = false;
                    }
                    latch.countDown();
                }

                @Override
                public void onException(Command request, Throwable cause) {
                    result[0] = false;
                    latch.countDown();
                }
            });
        }

        if (!latch.await(config.getTransactionSyncTimeout(), TimeUnit.MILLISECONDS)) {
            logger.error("commit transaction timeout, metadata: {}", prepareList);
            return false;
        }

        if (result[0]) {
            writeMarker(prepareList.get(0), TransactionState.COMPLETE_COMMIT);
        }

        return result[0];
    }

    public boolean prepareAbort(List<TransactionPrepare> prepareList) throws Exception {
        return writeMarker(prepareList.get(0), TransactionState.PREPARE_ABORT);
    }

    public boolean abort(List<TransactionPrepare> prepareList) throws Exception {
        prepareList = filterPrepareByBroker(prepareList);
        CountDownLatch latch = new CountDownLatch(prepareList.size());
        boolean[] result = {true};

        for (TransactionPrepare prepare : prepareList) {
            CoordinatorSession session = sessionManager.getOrCreateSession(prepare.getBrokerId(), prepare.getBrokerHost(), prepare.getBrokerPort());
            String txId = transactionIdManager.generateId(prepare.getTopic(), prepare.getApp(), prepare.getTransactionId(), prepare.getProducerId(), prepare.getProducerEpoch());
            TransactionRollbackRequest transactionRollbackRequest = new TransactionRollbackRequest(prepare.getTopic(), prepare.getApp(), txId);
            session.async(new JMQCommand(transactionRollbackRequest), new CommandCallback() {
                @Override
                public void onSuccess(Command request, Command response) {
                    if (response.getHeader().getStatus() != JMQCode.CN_TRANSACTION_NOT_EXISTS.getCode()) {
                        result[0] = false;
                    }
                    latch.countDown();
                }

                @Override
                public void onException(Command request, Throwable cause) {
                    result[0] = false;
                    latch.countDown();
                }
            });
        }

        if (!latch.await(config.getTransactionSyncTimeout(), TimeUnit.MILLISECONDS)) {
            logger.error("abort transaction timeout, metadata: {}", prepareList);
            return false;
        }

        if (result[0]) {
            writeMarker(prepareList.get(0), TransactionState.COMPLETE_ABORT);
        }

        return result[0];
    }

    protected boolean writeMarker(TransactionPrepare prepare, TransactionState state) throws Exception {
        TransactionMarker marker = convertMarker(prepare, state);
        return transactionLog.writeMarker(marker);
    }

    protected TransactionMarker convertMarker(TransactionPrepare prepare, TransactionState state) {
        TransactionMarker marker = new TransactionMarker(prepare.getTopic(), prepare.getPartition(), prepare.getApp(), prepare.getBrokerId(), prepare.getBrokerHost(), prepare.getBrokerPort(),
                prepare.getTransactionId(), prepare.getProducerId(), prepare.getProducerEpoch(), state, prepare.getTimeout(), SystemClock.now());
        return marker;
    }

    protected List<TransactionPrepare> filterPrepareByBroker(List<TransactionPrepare> prepareList) {
        Table<Integer, String, Boolean> brokerTopicTable = HashBasedTable.create();
        List<TransactionPrepare> result = Lists.newLinkedList();
        for (TransactionPrepare prepare : prepareList) {
            if (brokerTopicTable.contains(prepare.getBrokerId(), prepare.getTopic())) {
                continue;
            }
            brokerTopicTable.put(prepare.getBrokerId(), prepare.getTopic(), true);
            result.add(prepare);
        }
        return result;
    }
}