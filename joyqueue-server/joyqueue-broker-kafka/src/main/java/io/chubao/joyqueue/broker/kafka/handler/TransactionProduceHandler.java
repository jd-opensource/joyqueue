package io.chubao.joyqueue.broker.kafka.handler;

import io.chubao.joyqueue.broker.kafka.KafkaErrorCode;
import io.chubao.joyqueue.broker.kafka.command.ProduceRequest;
import io.chubao.joyqueue.broker.kafka.command.ProduceResponse;
import io.chubao.joyqueue.broker.kafka.config.KafkaConfig;
import io.chubao.joyqueue.broker.kafka.coordinator.transaction.TransactionCoordinator;
import io.chubao.joyqueue.broker.kafka.coordinator.transaction.TransactionIdManager;
import io.chubao.joyqueue.broker.kafka.model.ProducePartitionGroupRequest;
import io.chubao.joyqueue.broker.producer.Produce;
import io.chubao.joyqueue.domain.QosLevel;
import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.message.BrokerMessage;
import io.chubao.joyqueue.message.BrokerPrepare;
import io.chubao.joyqueue.message.BrokerRollback;
import io.chubao.joyqueue.message.SourceType;
import io.chubao.joyqueue.network.session.Producer;
import io.chubao.joyqueue.network.session.TransactionId;
import io.chubao.joyqueue.toolkit.concurrent.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * TransactionProduceHandler
 *
 * author: gaohaoxiang
 * date: 2018/11/6
 */
public class TransactionProduceHandler {

    protected static final Logger logger = LoggerFactory.getLogger(TransactionProduceHandler.class);

    private KafkaConfig config;
    private Produce produce;
    private TransactionCoordinator transactionCoordinator;
    private TransactionIdManager transactionIdManager;

    public TransactionProduceHandler(KafkaConfig config, Produce produce, TransactionCoordinator transactionCoordinator,
                                     TransactionIdManager transactionIdManager) {
        this.config = config;
        this.produce = produce;
        this.transactionCoordinator = transactionCoordinator;
        this.transactionIdManager = transactionIdManager;
    }

    public void produceMessage(ProduceRequest request, String transactionalId, long producerId, short producerEpoch,
                               QosLevel qosLevel, Producer producer, ProducePartitionGroupRequest partitionGroupRequest,
                               EventListener<ProduceResponse.PartitionResponse> listener) {

        short[] code = {KafkaErrorCode.NONE.getCode()};
        CountDownLatch latch = new CountDownLatch(partitionGroupRequest.getMessageMap().size());

        for (Map.Entry<Integer, List<BrokerMessage>> entry : partitionGroupRequest.getMessageMap().entrySet()) {
            try {
                int partition = entry.getKey();
                List<BrokerMessage> messages = entry.getValue();
                String txId = generateTxId(producer, partition, transactionalId, producerId, producerEpoch);
                TransactionId transaction = tryPrepare(producer, txId);
                fillTxId(messages, transaction.getTxId());

                produce.putMessageAsync(producer, messages, qosLevel, (writeResult) -> {
                    if (!writeResult.getCode().equals(JoyQueueCode.SUCCESS)) {
                        logger.error("produce message failed, topic: {}, code: {}", producer.getTopic(), writeResult.getCode());
                    }
                    code[0] = KafkaErrorCode.joyQueueCodeFor(writeResult.getCode().getCode());
                    latch.countDown();
                });
            } catch (Exception e) {
                logger.error("produce message failed, topic: {}", producer.getTopic(), e);
                code[0] = KafkaErrorCode.exceptionFor(e);
                latch.countDown();
            }
        }

        try {
            if (!latch.await(Math.min(request.getAckTimeoutMs(), config.getProduceTimeout()), TimeUnit.MILLISECONDS)) {
                logger.warn("produce message timeout, topic: {}, request: {}", producer.getTopic(), partitionGroupRequest);
                code[0] = KafkaErrorCode.KAFKA_STORAGE_ERROR.getCode();
            }
        } catch (InterruptedException e) {
            logger.error("produce message failed, topic: {}", producer.getTopic(), e);
            code[0] = KafkaErrorCode.KAFKA_STORAGE_ERROR.getCode();
        }
        listener.onEvent(new ProduceResponse.PartitionResponse(ProduceResponse.PartitionResponse.NONE_OFFSET, code[0]));
    }

    protected String generateTxId(Producer producer, int partition, String transactionalId, long producerId, short producerEpoch) {
        return transactionIdManager.generateId(producer.getTopic(), partition, producer.getApp(), transactionalId, producerId, producerEpoch);
    }

    protected TransactionId tryPrepare(Producer producer, String txId) throws Exception {
        TransactionId transaction = produce.getTransaction(producer, txId);
        if (transaction == null) {
            transaction = prepare(producer, txId);
        }
        return transaction;
    }

    protected TransactionId prepare(Producer producer, String txId) throws Exception {
        BrokerPrepare brokerPrepare = new BrokerPrepare();
        brokerPrepare.setTopic(producer.getTopic());
        brokerPrepare.setApp(producer.getApp());
        brokerPrepare.setTxId(txId);
        brokerPrepare.setTimeout(config.getTransactionTimeout());
        brokerPrepare.setSource(SourceType.KAFKA.getValue());
        return produce.putTransactionMessage(producer, brokerPrepare);
    }

    protected TransactionId rollback(Producer producer, String txId) throws Exception {
        BrokerRollback brokerRollback = new BrokerRollback();
        brokerRollback.setTopic(producer.getTopic());
        brokerRollback.setApp(producer.getApp());
        brokerRollback.setTxId(txId);
        return produce.putTransactionMessage(producer, brokerRollback);
    }

    protected void fillTxId(List<BrokerMessage> messages, String txId) {
        for (BrokerMessage message : messages) {
            message.setTxId(txId);
        }
    }
}