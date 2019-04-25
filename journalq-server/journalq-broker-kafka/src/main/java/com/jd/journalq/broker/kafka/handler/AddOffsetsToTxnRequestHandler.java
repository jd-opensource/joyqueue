package com.jd.journalq.broker.kafka.handler;

import com.jd.journalq.broker.kafka.KafkaCommandHandler;
import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.KafkaContext;
import com.jd.journalq.broker.kafka.KafkaContextAware;
import com.jd.journalq.broker.kafka.KafkaErrorCode;
import com.jd.journalq.broker.kafka.command.AddOffsetsToTxnRequest;
import com.jd.journalq.broker.kafka.command.AddOffsetsToTxnResponse;
import com.jd.journalq.broker.kafka.coordinator.transaction.TransactionCoordinator;
import com.jd.journalq.broker.kafka.coordinator.transaction.exception.TransactionException;
import com.jd.journalq.broker.kafka.helper.KafkaClientHelper;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AddOffsetsToTxnRequestHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/4
 */
public class AddOffsetsToTxnRequestHandler implements KafkaCommandHandler, Type, KafkaContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(AddOffsetsToTxnRequestHandler.class);

    private TransactionCoordinator transactionCoordinator;

    @Override
    public void setKafkaContext(KafkaContext kafkaContext) {
        this.transactionCoordinator = kafkaContext.getTransactionCoordinator();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        AddOffsetsToTxnRequest addOffsetsToTxnRequest = (AddOffsetsToTxnRequest) command.getPayload();
        String clientId = KafkaClientHelper.parseClient(addOffsetsToTxnRequest.getClientId());
        AddOffsetsToTxnResponse response = null;

        try {
            boolean isSuccess = transactionCoordinator.handleAddOffsetsToTxn(clientId, addOffsetsToTxnRequest.getTransactionId(), addOffsetsToTxnRequest.getGroupId(), addOffsetsToTxnRequest.getProducerId(), addOffsetsToTxnRequest.getProducerEpoch());
            response = new AddOffsetsToTxnResponse(KafkaErrorCode.NONE.getCode());
        } catch (TransactionException e) {
            logger.warn("add offsets to txn exception, code: {}, message: {}, request: {}, code: {}", e.getCode(), e.getMessage(), addOffsetsToTxnRequest, e.getCode());
            response = new AddOffsetsToTxnResponse((short) e.getCode());
        } catch (Exception e) {
            logger.error("add offsets to txn exception, request: {}", addOffsetsToTxnRequest, e);
            response = new AddOffsetsToTxnResponse(KafkaErrorCode.exceptionFor(e));
        }

        return new Command(response);
    }

    @Override
    public int type() {
        return KafkaCommandType.ADD_OFFSETS_TO_TXN.getCode();
    }
}