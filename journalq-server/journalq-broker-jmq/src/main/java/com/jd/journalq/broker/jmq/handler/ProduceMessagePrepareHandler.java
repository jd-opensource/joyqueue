package com.jd.journalq.broker.jmq.handler;

import com.jd.journalq.broker.jmq.JMQCommandHandler;
import com.jd.journalq.broker.jmq.converter.CheckResultConverter;
import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.BrokerContextAware;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.broker.helper.SessionHelper;
import com.jd.journalq.broker.producer.Produce;
import com.jd.journalq.common.domain.TopicName;
import com.jd.journalq.common.exception.JMQCode;
import com.jd.journalq.common.exception.JMQException;
import com.jd.journalq.common.message.BrokerPrepare;
import com.jd.journalq.common.network.command.BooleanAck;
import com.jd.journalq.common.network.command.JMQCommandType;
import com.jd.journalq.common.network.command.ProduceMessagePrepare;
import com.jd.journalq.common.network.command.ProduceMessagePrepareAck;
import com.jd.journalq.common.network.session.Connection;
import com.jd.journalq.common.network.session.Producer;
import com.jd.journalq.common.network.transport.Transport;
import com.jd.journalq.common.network.transport.command.Command;
import com.jd.journalq.common.network.transport.command.Type;
import com.jd.journalq.common.response.BooleanResponse;
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

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.produce = brokerContext.getProduce();
        this.clusterManager = brokerContext.getClusterManager();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        ProduceMessagePrepare produceMessagePrepare = (ProduceMessagePrepare) command.getPayload();
        Connection connection = SessionHelper.getConnection(transport);

        if (connection == null || !connection.isAuthorized(produceMessagePrepare.getApp())) {
            logger.warn("connection is not exists, transport: {}", transport);
            return BooleanAck.build(JMQCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        BooleanResponse checkResult = clusterManager.checkWritable(TopicName.parse(produceMessagePrepare.getTopic()), produceMessagePrepare.getApp(), connection.getHost());
        if (!checkResult.isSuccess()) {
            logger.warn("checkWritable failed, transport: {}, topic: {}, app: {}", transport, produceMessagePrepare, produceMessagePrepare.getApp());
            return new Command(new ProduceMessagePrepareAck(CheckResultConverter.convertCommonCode(checkResult.getJmqCode())));
        }

        ProduceMessagePrepareAck produceMessagePrepareAck = produceMessagePrepare(connection, produceMessagePrepare);
        return new Command(produceMessagePrepareAck);
    }

    protected ProduceMessagePrepareAck produceMessagePrepare(Connection connection, ProduceMessagePrepare produceMessagePrepare) {
        String txId = generateTxId(connection, produceMessagePrepare.getTopic(), produceMessagePrepare.getApp(), produceMessagePrepare.getSequence());
        Producer producer = new Producer(txId, produceMessagePrepare.getTopic(), produceMessagePrepare.getApp(), Producer.ProducerType.JMQ);

        BrokerPrepare brokerPrepare = new BrokerPrepare();
        brokerPrepare.setTxId(txId);
        brokerPrepare.setTopic(produceMessagePrepare.getTopic());
        brokerPrepare.setApp(produceMessagePrepare.getApp());
        brokerPrepare.setQueryId(produceMessagePrepare.getTransactionId());
        brokerPrepare.setTimeout(produceMessagePrepare.getTimeout());

        try {
            produce.putTransactionMessage(producer, brokerPrepare);
            return new ProduceMessagePrepareAck(txId, JMQCode.SUCCESS);
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

    protected String generateTxId(Connection connection, String topic, String app, long sequence) {
        return connection.getId() + "_" + topic + "_" + app + "_" + sequence;
    }

    @Override
    public int type() {
        return JMQCommandType.PRODUCE_MESSAGE_PREPARE.getCode();
    }
}