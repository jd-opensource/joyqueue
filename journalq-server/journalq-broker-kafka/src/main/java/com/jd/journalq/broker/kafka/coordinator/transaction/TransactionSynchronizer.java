package com.jd.journalq.broker.kafka.coordinator.transaction;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.jd.journalq.broker.coordinator.session.CoordinatorSession;
import com.jd.journalq.broker.coordinator.session.CoordinatorSessionManager;
import com.jd.journalq.broker.kafka.config.KafkaConfig;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionMetadata;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionPrepare;
import com.jd.journalq.broker.producer.transaction.command.TransactionCommitRequest;
import com.jd.journalq.broker.producer.transaction.command.TransactionRollbackRequest;
import com.jd.journalq.domain.Broker;
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
// TODO 返回值处理
public class TransactionSynchronizer extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(TransactionSynchronizer.class);

    private KafkaConfig config;
    private TransactionIdManager transactionIdManager;
    private CoordinatorSessionManager sessionManager;

    public TransactionSynchronizer(KafkaConfig config, TransactionIdManager transactionIdManager, CoordinatorSessionManager sessionManager) {
        this.config = config;
        this.transactionIdManager = transactionIdManager;
        this.sessionManager = sessionManager;
    }

    // TODO 写事务日志
    public void prepare(TransactionMetadata transactionMetadata, String topic, int partition, Broker broker) throws Exception {
        TransactionPrepare transactionPrepare = new TransactionPrepare(topic, (short) partition, transactionMetadata.getApp(), broker.getId(), broker.getIp(), broker.getBackEndPort(),
                transactionMetadata.getId(), transactionMetadata.getProducerId(), transactionMetadata.getProducerEpoch(), transactionMetadata.getTimeout(), SystemClock.now());

        transactionMetadata.addPrepare(transactionPrepare);
    }

    // TODO 写事务日志
    public void commit(TransactionMetadata transactionMetadata) throws Exception {
        List<TransactionPrepare> prepareList = filterPrepareByBroker(transactionMetadata.getPrepare());
        CountDownLatch latch = new CountDownLatch(prepareList.size());
        for (TransactionPrepare prepare : prepareList) {
            CoordinatorSession session = sessionManager.getOrCreateSession(prepare.getBrokerId(), prepare.getBrokerHost(), prepare.getBrokerPort());
            String txId = transactionIdManager.generateId(prepare.getTopic(), prepare.getApp(), prepare.getTransactionId(), prepare.getProducerId(), prepare.getProducerEpoch());
            TransactionCommitRequest transactionCommitRequest = new TransactionCommitRequest(prepare.getTopic(), prepare.getApp(), txId);
            session.async(new JMQCommand(transactionCommitRequest), new CommandCallback() {
                @Override
                public void onSuccess(Command request, Command response) {
                    latch.countDown();
                }

                @Override
                public void onException(Command request, Throwable cause) {
                    latch.countDown();
                }
            });
        }

        if (!latch.await(config.getTransactionSyncTimeout(), TimeUnit.MILLISECONDS)) {
            logger.error("commit transaction timeout, metadata: {}", transactionMetadata);
        }
    }

    // TODO 写事务日志
    public void abort(TransactionMetadata transactionMetadata) throws Exception {
        List<TransactionPrepare> prepareList = filterPrepareByBroker(transactionMetadata.getPrepare());
        CountDownLatch latch = new CountDownLatch(prepareList.size());
        for (TransactionPrepare prepare : prepareList) {
            CoordinatorSession session = sessionManager.getOrCreateSession(prepare.getBrokerId(), prepare.getBrokerHost(), prepare.getBrokerPort());
            String txId = transactionIdManager.generateId(prepare.getTopic(), prepare.getApp(), prepare.getTransactionId(), prepare.getProducerId(), prepare.getProducerEpoch());
            TransactionRollbackRequest transactionRollbackRequest = new TransactionRollbackRequest(prepare.getTopic(), prepare.getApp(), txId);
            session.async(new JMQCommand(transactionRollbackRequest), new CommandCallback() {
                @Override
                public void onSuccess(Command request, Command response) {
                    latch.countDown();
                }

                @Override
                public void onException(Command request, Throwable cause) {
                    latch.countDown();
                }
            });
        }

        if (!latch.await(config.getTransactionSyncTimeout(), TimeUnit.MILLISECONDS)) {
            logger.error("abort transaction timeout, metadata: {}", transactionMetadata);
        }
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