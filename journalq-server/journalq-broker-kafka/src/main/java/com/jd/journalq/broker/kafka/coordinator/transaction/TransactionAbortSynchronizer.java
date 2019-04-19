package com.jd.journalq.broker.kafka.coordinator.transaction;

import com.jd.journalq.broker.coordinator.session.CoordinatorSession;
import com.jd.journalq.broker.coordinator.session.CoordinatorSessionManager;
import com.jd.journalq.broker.kafka.config.KafkaConfig;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionMetadata;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionPrepare;
import com.jd.journalq.broker.kafka.coordinator.transaction.helper.TransactionHelper;
import com.jd.journalq.broker.producer.transaction.command.TransactionRollbackRequest;
import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.CommandCallback;
import com.jd.journalq.network.transport.command.JMQCommand;
import com.jd.journalq.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * TransactionAbortSynchronizer
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/18
 */
// TODO 补充日志
public class TransactionAbortSynchronizer extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(TransactionAbortSynchronizer.class);

    private KafkaConfig config;
    private CoordinatorSessionManager sessionManager;
    private TransactionIdManager transactionIdManager;

    public TransactionAbortSynchronizer(KafkaConfig config, CoordinatorSessionManager sessionManager, TransactionIdManager transactionIdManager) {
        this.config = config;
        this.sessionManager = sessionManager;
        this.transactionIdManager = transactionIdManager;
    }

    public boolean abort(TransactionMetadata transactionMetadata, Set<TransactionPrepare> prepareList) throws Exception {
        prepareList = TransactionHelper.filterPrepareByBroker(prepareList);
        CountDownLatch latch = new CountDownLatch(prepareList.size());
        boolean[] result = {true};

        for (TransactionPrepare prepare : prepareList) {
            CoordinatorSession session = sessionManager.getOrCreateSession(prepare.getBrokerId(), prepare.getBrokerHost(), prepare.getBrokerPort());
            String txId = transactionIdManager.generateId(prepare.getTopic(), prepare.getApp(), prepare.getTransactionId(), prepare.getProducerId(), prepare.getProducerEpoch());
            TransactionRollbackRequest transactionRollbackRequest = new TransactionRollbackRequest(prepare.getTopic(), prepare.getApp(), txId);
            session.async(new JMQCommand(transactionRollbackRequest), new CommandCallback() {
                @Override
                public void onSuccess(Command request, Command response) {
                    if (response.getHeader().getStatus() != JMQCode.SUCCESS.getCode() &&
                            response.getHeader().getStatus() != JMQCode.CN_TRANSACTION_NOT_EXISTS.getCode()) {
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

        return result[0];
    }
}