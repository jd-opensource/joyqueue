package com.jd.joyqueue.broker.jmq2.handler;

import com.jd.joyqueue.broker.jmq2.JMQ2CommandHandler;
import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;
import com.jd.joyqueue.broker.jmq2.command.BooleanAck;
import com.jd.joyqueue.broker.jmq2.command.TxCommit;
import com.jd.joyqueue.broker.jmq2.command.TxPrepare;
import com.jd.joyqueue.broker.jmq2.command.TxRollback;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.BrokerContextAware;
import org.joyqueue.broker.monitor.SessionManager;
import org.joyqueue.broker.producer.Produce;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.exception.JoyQueueException;
import org.joyqueue.message.BrokerCommit;
import org.joyqueue.message.BrokerMessage;
import org.joyqueue.message.BrokerPrepare;
import org.joyqueue.message.BrokerRollback;
import org.joyqueue.message.Message;
import org.joyqueue.network.session.Producer;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.Types;
import org.joyqueue.network.transport.exception.TransportException;
import org.joyqueue.toolkit.network.IpUtil;
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 事务处理
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/31
 */
@Deprecated
public class TransactionHandler implements JMQ2CommandHandler, Types, BrokerContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(TransactionHandler.class);

    private SessionManager sessionManager;
    private Produce produce;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.sessionManager = brokerContext.getSessionManager();
        this.produce = brokerContext.getProduce();
    }

    @Override
    public Command handle(Transport transport, Command command) throws TransportException {
        Object payload = command.getPayload();
        if (payload instanceof TxCommit) {
            return txCommit(transport, command, (TxCommit) payload);
        } else if (payload instanceof TxPrepare) {
            return txPrepare(transport, command, (TxPrepare) payload);
        } else if (payload instanceof TxRollback) {
            return txRollback(transport, command, (TxRollback) payload);
        } else {
            throw new TransportException.RequestErrorException(JoyQueueCode.CN_COMMAND_UNSUPPORTED.getMessage(payload.getClass()));
        }
    }

    protected Command txCommit(Transport transport, Command command, TxCommit txCommit) {
        Producer producer = sessionManager.getProducerById(txCommit.getTransactionId().getProducerId());

        if (producer == null) {
            logger.warn("producer session is not exist, transport: {}, txCommit: {}", transport, txCommit);
            return BooleanAck.build(JoyQueueCode.FW_PRODUCER_NOT_EXISTS);
        }

        BrokerCommit brokerCommit = new BrokerCommit();
        brokerCommit.setTxId(txCommit.getTransactionId().getTxId());
        brokerCommit.setTopic(producer.getTopic());
        brokerCommit.setApp(producer.getApp());
        brokerCommit.setStartTime(SystemClock.now());

        try {
            produce.putTransactionMessage(producer, brokerCommit);
            return BooleanAck.build();
        } catch (JoyQueueException e) {
            logger.error("txCommit exception, transport: {}, producer: {}, txCommit: {}", transport, producer, txCommit, e);
            return BooleanAck.build(e.getCode(), e.getMessage());
        }
    }

    protected Command txPrepare(Transport transport, Command command, TxPrepare txPrepare) {
        Producer producer = sessionManager.getProducerById(txPrepare.getTransactionId().getProducerId());

        if (producer == null) {
            logger.warn("producer session is not exist, transport: {}, txPrepare: {}", transport, txPrepare);
            return BooleanAck.build(JoyQueueCode.FW_PRODUCER_NOT_EXISTS);
        }

        BrokerPrepare brokerPrepare = new BrokerPrepare();
        brokerPrepare.setTxId(txPrepare.getTransactionId().getTxId());
        brokerPrepare.setTimeout(txPrepare.getTimeout());
        brokerPrepare.setTopic(producer.getTopic());
        brokerPrepare.setApp(producer.getApp());

        for (Message message : txPrepare.getMessages()) {
            BrokerMessage brokerMessage = (BrokerMessage) message;
            brokerMessage.setClientIp(IpUtil.toByte((InetSocketAddress) transport.remoteAddress()));
        }

        try {
            produce.putTransactionMessage(producer, brokerPrepare);
            produce.putMessage(producer, (List) txPrepare.getMessages(), command.getHeader().getQosLevel());
            return BooleanAck.build();
        } catch (JoyQueueException e) {
            logger.error("txPrepare exception, transport: {}, producer: {}, txPrepare: {}", transport, producer, txPrepare, e);
            return BooleanAck.build(e.getCode(), e.getMessage());
        }
    }

    protected Command txRollback(Transport transport, Command command, TxRollback txRollback) {
        Producer producer = sessionManager.getProducerById(txRollback.getTransactionId().getProducerId());

        if (producer == null) {
            logger.warn("producer session is not exist, transport: {}, txRollback: {}", transport, txRollback);
            return BooleanAck.build(JoyQueueCode.FW_PRODUCER_NOT_EXISTS);
        }

        BrokerRollback brokerRollback = new BrokerRollback();
        brokerRollback.setTopic(producer.getTopic());
        brokerRollback.setApp(producer.getApp());
        brokerRollback.setTxId(txRollback.getTransactionId().getTxId());

        try {
            produce.putTransactionMessage(producer, brokerRollback);
            return BooleanAck.build();
        } catch (JoyQueueException e) {
            logger.error("txRollback exception, transport: {}, producer: {}, txRollback: {}", transport, producer, txRollback, e);
            return BooleanAck.build(e.getCode(), e.getMessage());
        }
    }

    @Override
    public int[] types() {
        return new int[] {JMQ2CommandType.COMMIT.getCode(), JMQ2CommandType.PREPARE.getCode(), JMQ2CommandType.ROLLBACK.getCode()};
    }
}