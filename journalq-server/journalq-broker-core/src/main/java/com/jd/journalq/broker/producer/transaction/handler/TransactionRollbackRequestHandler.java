package com.jd.journalq.broker.producer.transaction.handler;

import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.producer.Produce;
import com.jd.journalq.broker.producer.transaction.command.TransactionRollbackRequest;
import com.jd.journalq.exception.JournalqCode;
import com.jd.journalq.exception.JournalqException;
import com.jd.journalq.message.BrokerRollback;
import com.jd.journalq.network.command.BooleanAck;
import com.jd.journalq.network.command.CommandType;
import com.jd.journalq.network.session.Producer;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.network.transport.command.handler.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TransactionRollbackRequestHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/12
 */
public class TransactionRollbackRequestHandler implements CommandHandler, Type {

    protected static final Logger logger = LoggerFactory.getLogger(TransactionRollbackRequestHandler.class);

    private Produce produce;

    public TransactionRollbackRequestHandler(BrokerContext brokerContext) {
        this.produce = brokerContext.getProduce();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        TransactionRollbackRequest transactionRollbackRequest = (TransactionRollbackRequest) command.getPayload();
        Producer producer = new Producer(transactionRollbackRequest.getTopic(), transactionRollbackRequest.getTopic(), transactionRollbackRequest.getApp(), Producer.ProducerType.JMQ);
        int code = JournalqCode.SUCCESS.getCode();

        for (String txId : transactionRollbackRequest.getTxIds()) {
            BrokerRollback brokerRollback = new BrokerRollback();
            brokerRollback.setTopic(transactionRollbackRequest.getTopic());
            brokerRollback.setApp(transactionRollbackRequest.getApp());
            brokerRollback.setTxId(txId);

            try {
                produce.putTransactionMessage(producer, brokerRollback);
            } catch (JournalqException e) {
                if (e.getCode() == JournalqCode.CN_TRANSACTION_NOT_EXISTS.getCode()) {
                    logger.error("rollback transaction error, transaction not exists, topic: {}, app: {}, txId: {}", transactionRollbackRequest.getTopic(), transactionRollbackRequest.getApp(), txId);
                } else {
                    logger.error("rollback transaction exception, topic: {}, app: {}, txId: {}", transactionRollbackRequest.getTopic(), transactionRollbackRequest.getApp(), txId, e);
                }
                if (e.getCode() != JournalqCode.SUCCESS.getCode()) {
                    code = e.getCode();
                }
            } catch (Exception e) {
                logger.error("rollback transaction exception, topic: {}, app: {}, txId: {}", transactionRollbackRequest.getTopic(), transactionRollbackRequest.getApp(), txId, e);
                code = JournalqCode.CN_UNKNOWN_ERROR.getCode();
            }
        }

        return BooleanAck.build(code);
    }

    @Override
    public int type() {
        return CommandType.TRANSACTION_ROLLBACK_REQUEST;
    }
}