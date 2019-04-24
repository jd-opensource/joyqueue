package com.jd.journalq.broker.kafka.handler;

import com.jd.journalq.broker.kafka.KafkaErrorCode;
import com.jd.journalq.broker.kafka.command.ProduceResponse;
import com.jd.journalq.broker.kafka.config.KafkaConfig;
import com.jd.journalq.broker.kafka.coordinator.transaction.TransactionCoordinator;
import com.jd.journalq.broker.kafka.coordinator.transaction.TransactionIdManager;
import com.jd.journalq.broker.producer.Produce;
import com.jd.journalq.domain.QosLevel;
import com.jd.journalq.exception.JournalqCode;
import com.jd.journalq.message.BrokerMessage;
import com.jd.journalq.message.BrokerPrepare;
import com.jd.journalq.message.SourceType;
import com.jd.journalq.network.session.Producer;
import com.jd.journalq.network.session.TransactionId;
import com.jd.journalq.toolkit.concurrent.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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

    public TransactionProduceHandler(KafkaConfig config, Produce produce, TransactionCoordinator transactionCoordinator, TransactionIdManager transactionIdManager) {
        this.config = config;
        this.produce = produce;
        this.transactionCoordinator = transactionCoordinator;
        this.transactionIdManager = transactionIdManager;
    }

    public void produceMessage(String transactionalId, long producerId, short producerEpoch, QosLevel qosLevel, Producer producer, List<BrokerMessage> messages,
                               EventListener<ProduceResponse.PartitionResponse> listener) {
        try {
            TransactionId transaction = tryPrepare(producer, transactionalId, producerId, producerEpoch);
            fillTxId(messages, transaction.getTxId());

            produce.putMessageAsync(producer, messages, qosLevel, (writeResult) -> {
                if (!writeResult.getCode().equals(JournalqCode.SUCCESS)) {
                    logger.error("produce message failed, topic: {}, code: {}", producer.getTopic(), writeResult.getCode());
                }
                short code = KafkaErrorCode.journalqCodeFor(writeResult.getCode().getCode());
                listener.onEvent(new ProduceResponse.PartitionResponse(0, ProduceResponse.PartitionResponse.NONE_OFFSET, code));
            });
        } catch (Exception e) {
            logger.error("produce message failed, topic: {}", producer.getTopic(), e);
            short code = KafkaErrorCode.exceptionFor(e);
            listener.onEvent(new ProduceResponse.PartitionResponse(0, ProduceResponse.PartitionResponse.NONE_OFFSET, code));
        }
    }

    protected TransactionId tryPrepare(Producer producer, String transactionalId, long producerId, short producerEpoch) throws Exception {
        String txId = transactionIdManager.generateId(producer.getTopic(), producer.getApp(), transactionalId, producerId, producerEpoch);
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

    protected void fillTxId(List<BrokerMessage> messages, String txId) {
        for (BrokerMessage message : messages) {
            message.setTxId(txId);
        }
    }
}