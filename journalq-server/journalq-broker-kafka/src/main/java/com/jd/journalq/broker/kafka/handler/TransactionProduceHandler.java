package com.jd.journalq.broker.kafka.handler;

import com.jd.journalq.broker.kafka.KafkaErrorCode;
import com.jd.journalq.broker.kafka.command.ProduceRequest;
import com.jd.journalq.broker.kafka.command.ProduceResponse;
import com.jd.journalq.broker.kafka.config.KafkaConfig;
import com.jd.journalq.broker.kafka.coordinator.transaction.TransactionCoordinator;
import com.jd.journalq.broker.kafka.coordinator.transaction.TransactionIdManager;
import com.jd.journalq.broker.kafka.coordinator.transaction.TransactionProducerSequenceManager;
import com.jd.journalq.broker.kafka.model.ProducePartitionGroupRequest;
import com.jd.journalq.broker.producer.Produce;
import com.jd.journalq.domain.QosLevel;
import com.jd.journalq.exception.JournalqCode;
import com.jd.journalq.message.BrokerMessage;
import com.jd.journalq.message.BrokerPrepare;
import com.jd.journalq.message.BrokerRollback;
import com.jd.journalq.message.SourceType;
import com.jd.journalq.network.session.Producer;
import com.jd.journalq.network.session.TransactionId;
import com.jd.journalq.toolkit.concurrent.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * TransactionProduceHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/6
 */
public class TransactionProduceHandler {

    protected static final Logger logger = LoggerFactory.getLogger(TransactionProduceHandler.class);

    private KafkaConfig config;
    private Produce produce;
    private TransactionCoordinator transactionCoordinator;
    private TransactionIdManager transactionIdManager;
    private TransactionProducerSequenceManager transactionProducerSequenceManager;

    public TransactionProduceHandler(KafkaConfig config, Produce produce, TransactionCoordinator transactionCoordinator,
                                     TransactionIdManager transactionIdManager, TransactionProducerSequenceManager transactionProducerSequenceManager) {
        this.config = config;
        this.produce = produce;
        this.transactionCoordinator = transactionCoordinator;
        this.transactionIdManager = transactionIdManager;
        this.transactionProducerSequenceManager = transactionProducerSequenceManager;
    }

    public void produceMessage(ProduceRequest request, String transactionalId, long producerId, short producerEpoch, QosLevel qosLevel, Producer producer, ProducePartitionGroupRequest partitionGroupRequest,
                               EventListener<ProduceResponse.PartitionResponse> listener) {

        short[] code = {KafkaErrorCode.NONE.getCode()};
        CountDownLatch latch = new CountDownLatch(partitionGroupRequest.getMessageMap().size());

        for (Map.Entry<Integer, List<BrokerMessage>> entry : partitionGroupRequest.getMessageMap().entrySet())
            try {
                int partition = entry.getKey();
                List<BrokerMessage> messages = entry.getValue();
                String txId = generateTxId(producer, partition, transactionalId, producerId, producerEpoch);
                TransactionId transaction = tryPrepare(producer, txId);
                fillTxId(messages, transaction.getTxId());

                produce.putMessageAsync(producer, messages, qosLevel, (writeResult) -> {
                    if (!writeResult.getCode().equals(JournalqCode.SUCCESS)) {
                        logger.error("produce message failed, topic: {}, code: {}", producer.getTopic(), writeResult.getCode());
                    }
                    code[0] = KafkaErrorCode.journalqCodeFor(writeResult.getCode().getCode());
                    latch.countDown();
                });
            } catch (Exception e) {
                logger.error("produce message failed, topic: {}", producer.getTopic(), e);
                code[0] = KafkaErrorCode.exceptionFor(e);
                latch.countDown();
            }

        try {
            latch.await(request.getAckTimeoutMs(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.error("produce message failed, topic: {}", producer.getTopic(), e);
        }
        listener.onEvent(new ProduceResponse.PartitionResponse(ProduceResponse.PartitionResponse.NONE_OFFSET, code[0]));
    }

    protected boolean checkSequence(long producerId, short producerEpoch, long sequence) {
        long lastSequence = transactionProducerSequenceManager.getSequence(producerId, producerEpoch);
        if (sequence == 0 || sequence == lastSequence + 1) {
            return true;
        }
        return false;
    }

    protected void updateSequence(long producerId, short producerEpoch, long sequence) {
        transactionProducerSequenceManager.updateSequence(producerId, producerEpoch, sequence);
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