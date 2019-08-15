package io.chubao.joyqueue.broker.kafka.coordinator.transaction.synchronizer;

import com.google.common.collect.Lists;
import io.chubao.joyqueue.broker.coordinator.session.CoordinatorSession;
import io.chubao.joyqueue.broker.coordinator.session.CoordinatorSessionManager;
import io.chubao.joyqueue.broker.kafka.config.KafkaConfig;
import io.chubao.joyqueue.broker.kafka.coordinator.transaction.TransactionIdManager;
import io.chubao.joyqueue.broker.kafka.coordinator.transaction.domain.TransactionMetadata;
import io.chubao.joyqueue.broker.kafka.coordinator.transaction.domain.TransactionPrepare;
import io.chubao.joyqueue.broker.kafka.coordinator.transaction.helper.TransactionHelper;
import io.chubao.joyqueue.broker.producer.transaction.command.TransactionRollbackRequest;
import io.chubao.joyqueue.domain.Broker;
import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.CommandCallback;
import io.chubao.joyqueue.network.transport.command.JoyQueueCommand;
import io.chubao.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * TransactionAbortSynchronizer
 *
 * author: gaohaoxiang
 * date: 2019/4/18
 */
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
        Map<Broker, List<TransactionPrepare>> brokerPrepareMap = TransactionHelper.splitPrepareByBroker(prepareList);
        CountDownLatch latch = new CountDownLatch(brokerPrepareMap.size());
        boolean[] result = {true};

        for (Map.Entry<Broker, List<TransactionPrepare>> entry : brokerPrepareMap.entrySet()) {
            Broker broker = entry.getKey();
            List<TransactionPrepare> brokerPrepareList = entry.getValue();
            TransactionPrepare brokerPrepare = brokerPrepareList.get(0);
            List<String> txIds = Lists.newLinkedList();

            for (TransactionPrepare prepare : brokerPrepareList) {
                String txId = transactionIdManager.generateId(prepare.getTopic(), prepare.getPartition(), prepare.getApp(),
                        prepare.getTransactionId(), prepare.getProducerId(), prepare.getProducerEpoch());
                txIds.add(txId);
            }

            CoordinatorSession session = sessionManager.getOrCreateSession(broker);
            TransactionRollbackRequest transactionRollbackRequest = new TransactionRollbackRequest(brokerPrepare.getTopic(), brokerPrepare.getApp(), txIds);
            session.async(new JoyQueueCommand(transactionRollbackRequest), new CommandCallback() {
                @Override
                public void onSuccess(Command request, Command response) {
                    if (response.getHeader().getStatus() != JoyQueueCode.SUCCESS.getCode() &&
                            response.getHeader().getStatus() != JoyQueueCode.CN_TRANSACTION_NOT_EXISTS.getCode()) {
                        logger.error("abort transaction error, broker: {}, request: {}", broker, transactionRollbackRequest);
                        result[0] = false;
                    }

                    latch.countDown();
                }

                @Override
                public void onException(Command request, Throwable cause) {
                    logger.error("abort transaction error, broker: {}, request: {}", broker, transactionRollbackRequest, cause);
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