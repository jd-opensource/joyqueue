package com.jd.journalq.broker.kafka.handler;

import com.jd.journalq.broker.kafka.KafkaCommandHandler;
import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.KafkaContext;
import com.jd.journalq.broker.kafka.KafkaContextAware;
import com.jd.journalq.broker.kafka.KafkaErrorCode;
import com.jd.journalq.broker.kafka.command.EndTxnRequest;
import com.jd.journalq.broker.kafka.command.EndTxnResponse;
import com.jd.journalq.broker.kafka.coordinator.transaction.TransactionCoordinator;
import com.jd.journalq.broker.kafka.coordinator.transaction.exception.CoordinatorTransactionException;
import com.jd.journalq.broker.kafka.helper.KafkaClientHelper;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EndTxnHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/4
 */
public class EndTxnHandler implements KafkaCommandHandler, Type, KafkaContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(EndTxnHandler.class);

    private TransactionCoordinator transactionCoordinator;

    @Override
    public void setKafkaContext(KafkaContext kafkaContext) {
        this.transactionCoordinator = kafkaContext.getTransactionCoordinator();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        EndTxnRequest endTxnRequest = (EndTxnRequest) command.getPayload();
        String clientId = KafkaClientHelper.parseClient(endTxnRequest.getClientId());
        EndTxnResponse response = null;

        try {
            boolean isSuccess = transactionCoordinator.handleEndTxn(clientId, endTxnRequest.getTransactionId(),
                    endTxnRequest.getProducerId(), endTxnRequest.getProducerEpoch(), endTxnRequest.isCommit());
            response = new EndTxnResponse(KafkaErrorCode.NONE.getCode());
        } catch (CoordinatorTransactionException e) {
            logger.warn("endTxn exception, code: {}, message: {}, request: {}", e.getCode(), e.getMessage(), endTxnRequest, e);
            response = new EndTxnResponse((short) e.getCode());
        } catch (Exception e) {
            logger.error("endTxn exception, request: {}", endTxnRequest, e);
            response = new EndTxnResponse(KafkaErrorCode.exceptionFor(e));
        }

        return new Command(response);
    }

    @Override
    public int type() {
        return KafkaCommandType.END_TXN.getCode();
    }
}