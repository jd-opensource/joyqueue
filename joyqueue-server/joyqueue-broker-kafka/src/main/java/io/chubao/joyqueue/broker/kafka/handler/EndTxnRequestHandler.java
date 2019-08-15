package io.chubao.joyqueue.broker.kafka.handler;

import io.chubao.joyqueue.broker.kafka.KafkaCommandHandler;
import io.chubao.joyqueue.broker.kafka.KafkaCommandType;
import io.chubao.joyqueue.broker.kafka.KafkaContext;
import io.chubao.joyqueue.broker.kafka.KafkaContextAware;
import io.chubao.joyqueue.broker.kafka.KafkaErrorCode;
import io.chubao.joyqueue.broker.kafka.command.EndTxnRequest;
import io.chubao.joyqueue.broker.kafka.command.EndTxnResponse;
import io.chubao.joyqueue.broker.kafka.coordinator.transaction.TransactionCoordinator;
import io.chubao.joyqueue.broker.kafka.coordinator.transaction.exception.TransactionException;
import io.chubao.joyqueue.broker.kafka.helper.KafkaClientHelper;
import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EndTxnRequestHandler
 *
 * author: gaohaoxiang
 * date: 2019/4/4
 */
public class EndTxnRequestHandler implements KafkaCommandHandler, Type, KafkaContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(EndTxnRequestHandler.class);

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
        } catch (TransactionException e) {
            logger.warn("endTxn exception, message: {}, request: {}, code: {}", e.getMessage(), endTxnRequest, e.getCode());
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