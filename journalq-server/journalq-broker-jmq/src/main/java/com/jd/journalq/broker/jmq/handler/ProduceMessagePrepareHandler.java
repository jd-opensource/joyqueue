package com.jd.journalq.broker.jmq.handler;

import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.BrokerContextAware;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.broker.helper.SessionHelper;
import com.jd.journalq.broker.jmq.JMQCommandHandler;
import com.jd.journalq.broker.jmq.converter.CheckResultConverter;
import com.jd.journalq.broker.monitor.SessionManager;
import com.jd.journalq.broker.producer.Produce;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.exception.JMQException;
import com.jd.journalq.message.BrokerPrepare;
import com.jd.journalq.network.command.BooleanAck;
import com.jd.journalq.network.command.JMQCommandType;
import com.jd.journalq.network.command.ProduceMessagePrepare;
import com.jd.journalq.network.command.ProduceMessagePrepareAck;
import com.jd.journalq.network.session.Connection;
import com.jd.journalq.network.session.Producer;
import com.jd.journalq.network.session.TransactionId;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.response.BooleanResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ProduceMessagePrepareHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/19
 */
public class ProduceMessagePrepareHandler implements JMQCommandHandler, Type, BrokerContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(ProduceMessagePrepareHandler.class);

    private Produce produce;
    private ClusterManager clusterManager;
    private SessionManager sessionManager;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.produce = brokerContext.getProduce();
        this.clusterManager = brokerContext.getClusterManager();
        this.sessionManager = brokerContext.getSessionManager();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        ProduceMessagePrepare produceMessagePrepare = (ProduceMessagePrepare) command.getPayload();
        Connection connection = SessionHelper.getConnection(transport);

        if (connection == null || !connection.isAuthorized(produceMessagePrepare.getApp())) {
            logger.warn("connection is not exists, transport: {}", transport);
            return BooleanAck.build(JMQCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        String producerId = connection.getProducer(produceMessagePrepare.getTopic(), produceMessagePrepare.getApp());
        Producer producer = (StringUtils.isBlank(producerId) ? null : sessionManager.getProducerById(producerId));
        if (producer == null) {
            logger.warn("producer is not exists, transport: {}", transport);
            return BooleanAck.build(JMQCode.FW_PRODUCER_NOT_EXISTS.getCode());
        }

        BooleanResponse checkResult = clusterManager.checkWritable(TopicName.parse(produceMessagePrepare.getTopic()), produceMessagePrepare.getApp(), connection.getHost());
        if (!checkResult.isSuccess()) {
            logger.warn("checkWritable failed, transport: {}, topic: {}, app: {}", transport, produceMessagePrepare, produceMessagePrepare.getApp());
            return new Command(new ProduceMessagePrepareAck(CheckResultConverter.convertCommonCode(checkResult.getJmqCode())));
        }

        ProduceMessagePrepareAck produceMessagePrepareAck = produceMessagePrepare(producer, connection, produceMessagePrepare);
        return new Command(produceMessagePrepareAck);
    }

    protected ProduceMessagePrepareAck produceMessagePrepare(Producer producer, Connection connection, ProduceMessagePrepare produceMessagePrepare) {
        BrokerPrepare brokerPrepare = new BrokerPrepare();
        brokerPrepare.setTopic(produceMessagePrepare.getTopic());
        brokerPrepare.setApp(produceMessagePrepare.getApp());
        brokerPrepare.setQueryId(produceMessagePrepare.getTransactionId());
        brokerPrepare.setTimeout(produceMessagePrepare.getTimeout());

        try {
            TransactionId transactionId = produce.putTransactionMessage(producer, brokerPrepare);
            return new ProduceMessagePrepareAck(transactionId.getTxId(), JMQCode.SUCCESS);
        } catch (JMQException e) {
            logger.error("produceMessage prepare exception, transport: {}, topic: {}, app: {}",
                    connection.getTransport().remoteAddress(), produceMessagePrepare.getTopic(), produceMessagePrepare.getApp(), e);
            return new ProduceMessagePrepareAck(JMQCode.valueOf(e.getCode()));
        } catch (Exception e) {
            logger.error("produceMessage prepare exception, transport: {}, topic: {}, app: {}",
                    connection.getTransport().remoteAddress(), produceMessagePrepare.getTopic(), produceMessagePrepare.getApp(), e);
            return new ProduceMessagePrepareAck(JMQCode.CN_UNKNOWN_ERROR);
        }
    }

    @Override
    public int type() {
        return JMQCommandType.PRODUCE_MESSAGE_PREPARE.getCode();
    }
}