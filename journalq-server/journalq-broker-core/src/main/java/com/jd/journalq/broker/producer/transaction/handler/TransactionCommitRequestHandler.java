package com.jd.journalq.broker.producer.transaction.handler;

import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.producer.Produce;
import com.jd.journalq.broker.producer.transaction.command.TransactionCommitRequest;
import com.jd.journalq.exception.JournalqCode;
import com.jd.journalq.exception.JournalqException;
import com.jd.journalq.message.BrokerCommit;
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
 * TransactionCommitRequestHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/12
 */
public class TransactionCommitRequestHandler implements CommandHandler, Type {

    protected static final Logger logger = LoggerFactory.getLogger(TransactionCommitRequestHandler.class);

    private Produce produce;

    public TransactionCommitRequestHandler(BrokerContext brokerContext) {
        this.produce = brokerContext.getProduce();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        TransactionCommitRequest transactionCommitRequest = (TransactionCommitRequest) command.getPayload();
        Producer producer = new Producer(transactionCommitRequest.getTopic(), transactionCommitRequest.getTopic(), transactionCommitRequest.getApp(), Producer.ProducerType.JMQ);

        BrokerCommit brokerCommit = new BrokerCommit();
        brokerCommit.setTopic(transactionCommitRequest.getTopic());
        brokerCommit.setApp(transactionCommitRequest.getApp());
        brokerCommit.setTxId(transactionCommitRequest.getTxId());

        try {
            produce.putTransactionMessage(producer, brokerCommit);
            return BooleanAck.build();
        } catch (JournalqException e) {
            logger.error("commit transaction exception, topic: {}, app: {}", transactionCommitRequest.getTopic(), transactionCommitRequest.getApp(), e);
            return BooleanAck.build(e.getCode());
        } catch (Exception e) {
            logger.error("commit transaction exception, topic: {}, app: {}", transactionCommitRequest.getTopic(), transactionCommitRequest.getApp(), e);
            return BooleanAck.build(JournalqCode.CN_UNKNOWN_ERROR);
        }
    }

    @Override
    public int type() {
        return CommandType.TRANSACTION_COMMIT_REQUEST;
    }
}