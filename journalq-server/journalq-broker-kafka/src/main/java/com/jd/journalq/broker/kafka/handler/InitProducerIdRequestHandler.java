package com.jd.journalq.broker.kafka.handler;

import com.jd.journalq.broker.kafka.KafkaCommandHandler;
import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.KafkaContext;
import com.jd.journalq.broker.kafka.KafkaContextAware;
import com.jd.journalq.broker.kafka.KafkaErrorCode;
import com.jd.journalq.broker.kafka.command.InitProducerIdRequest;
import com.jd.journalq.broker.kafka.command.InitProducerIdResponse;
import com.jd.journalq.broker.kafka.coordinator.transaction.TransactionCoordinator;
import com.jd.journalq.broker.kafka.coordinator.transaction.domain.TransactionMetadata;
import com.jd.journalq.broker.kafka.coordinator.transaction.exception.TransactionException;
import com.jd.journalq.broker.kafka.helper.KafkaClientHelper;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * InitProducerIdRequestHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/4
 */
public class InitProducerIdRequestHandler implements KafkaCommandHandler, Type, KafkaContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(InitProducerIdRequestHandler.class);

    private TransactionCoordinator transactionCoordinator;

    @Override
    public void setKafkaContext(KafkaContext kafkaContext) {
        this.transactionCoordinator = kafkaContext.getTransactionCoordinator();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        InitProducerIdRequest initProducerIdRequest = (InitProducerIdRequest) command.getPayload();
        String clientId = KafkaClientHelper.parseClient(initProducerIdRequest.getClientId());
        InitProducerIdResponse response = null;

        try {
            TransactionMetadata transactionMetadata = transactionCoordinator.handleInitProducer(clientId, initProducerIdRequest.getTransactionId(), initProducerIdRequest.getTransactionTimeout());
            response = new InitProducerIdResponse(KafkaErrorCode.NONE.getCode(), transactionMetadata.getProducerId(), transactionMetadata.getProducerEpoch());
        } catch (TransactionException e) {
            logger.warn("init producerId exception, code: {}, message: {}, request: {}", e.getCode(), e.getMessage(), initProducerIdRequest, e);
            response = new InitProducerIdResponse((short) e.getCode());
        } catch (Exception e) {
            logger.error("init producerId exception, request: {}", initProducerIdRequest, e);
            response = new InitProducerIdResponse(KafkaErrorCode.exceptionFor(e));
        }

        return new Command(response);
    }

    @Override
    public int type() {
        return KafkaCommandType.INIT_PRODUCER_ID.getCode();
    }
}