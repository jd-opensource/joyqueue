package com.jd.journalq.broker.jmq.handler;

import com.jd.journalq.broker.jmq.JMQCommandHandler;
import com.jd.journalq.broker.jmq.converter.CheckResultConverter;
import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.BrokerContextAware;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.broker.helper.SessionHelper;
import com.jd.journalq.broker.producer.Produce;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.exception.JMQException;
import com.jd.journalq.message.BrokerCommit;
import com.jd.journalq.network.command.BooleanAck;
import com.jd.journalq.network.command.JMQCommandType;
import com.jd.journalq.network.command.ProduceMessageRollbackRequest;
import com.jd.journalq.network.command.ProduceMessageRollbackResponse;
import com.jd.journalq.network.session.Connection;
import com.jd.journalq.network.session.Producer;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.response.BooleanResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ProduceMessageRollbackHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/19
 */
public class ProduceMessageRollbackHandler implements JMQCommandHandler, Type, BrokerContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(ProduceMessageRollbackHandler.class);

    private Produce produce;
    private ClusterManager clusterManager;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.produce = brokerContext.getProduce();
        this.clusterManager = brokerContext.getClusterManager();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        ProduceMessageRollbackRequest produceMessageRollbackRequest = (ProduceMessageRollbackRequest) command.getPayload();
        Connection connection = SessionHelper.getConnection(transport);

        if (connection == null || !connection.isAuthorized(produceMessageRollbackRequest.getApp())) {
            logger.warn("connection is not exists, transport: {}", transport);
            return BooleanAck.build(JMQCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        BooleanResponse checkResult = clusterManager.checkWritable(TopicName.parse(produceMessageRollbackRequest.getTopic()), produceMessageRollbackRequest.getApp(), connection.getHost());
        if (!checkResult.isSuccess()) {
            logger.warn("checkWritable failed, transport: {}, topic: {}, app: {}, code: {}", transport, produceMessageRollbackRequest.getTopic(), produceMessageRollbackRequest.getApp(), checkResult.getJmqCode());
            return new Command(new ProduceMessageRollbackResponse(CheckResultConverter.convertCommonCode(checkResult.getJmqCode())));
        }

        ProduceMessageRollbackResponse produceMessageRollbackResponse = produceMessageRollback(connection, produceMessageRollbackRequest);
        return new Command(produceMessageRollbackResponse);
    }

    protected ProduceMessageRollbackResponse produceMessageRollback(Connection connection, ProduceMessageRollbackRequest produceMessageRollbackRequest) {
        Producer producer = new Producer(connection.getId(), produceMessageRollbackRequest.getTopic(), produceMessageRollbackRequest.getApp(), Producer.ProducerType.JMQ);

        BrokerCommit brokerCommit = new BrokerCommit();
        brokerCommit.setTopic(produceMessageRollbackRequest.getTopic());
        brokerCommit.setApp(produceMessageRollbackRequest.getApp());
        brokerCommit.setTxId(produceMessageRollbackRequest.getTxId());

        try {
            produce.putTransactionMessage(producer, brokerCommit);
            return new ProduceMessageRollbackResponse(JMQCode.SUCCESS);
        } catch (JMQException e) {
            logger.error("produceMessage prepare exception, transport: {}, topic: {}, app: {}",
                    connection.getTransport().remoteAddress(), produceMessageRollbackRequest.getTopic(), produceMessageRollbackRequest.getApp(), e);
            return new ProduceMessageRollbackResponse(JMQCode.valueOf(e.getCode()));
        } catch (Exception e) {
            logger.error("produceMessage prepare exception, transport: {}, topic: {}, app: {}",
                    connection.getTransport().remoteAddress(), produceMessageRollbackRequest.getTopic(), produceMessageRollbackRequest.getApp(), e);
            return new ProduceMessageRollbackResponse(JMQCode.CN_UNKNOWN_ERROR);
        }
    }

    @Override
    public int type() {
        return JMQCommandType.PRODUCE_MESSAGE_ROLLBACK.getCode();
    }
}