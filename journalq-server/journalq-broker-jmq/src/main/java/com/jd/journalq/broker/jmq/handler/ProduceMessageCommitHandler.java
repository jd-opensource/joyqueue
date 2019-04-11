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
import com.jd.journalq.network.command.ProduceMessageCommitRequest;
import com.jd.journalq.network.command.ProduceMessageCommitAck;
import com.jd.journalq.network.session.Connection;
import com.jd.journalq.network.session.Producer;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.response.BooleanResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ProduceMessageCommitHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/19
 */
public class ProduceMessageCommitHandler implements JMQCommandHandler, Type, BrokerContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(ProduceMessageCommitHandler.class);

    private Produce produce;
    private ClusterManager clusterManager;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.produce = brokerContext.getProduce();
        this.clusterManager = brokerContext.getClusterManager();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        ProduceMessageCommitRequest produceMessageCommitRequest = (ProduceMessageCommitRequest) command.getPayload();
        Connection connection = SessionHelper.getConnection(transport);

        if (connection == null || !connection.isAuthorized(produceMessageCommitRequest.getApp())) {
            logger.warn("connection is not exists, transport: {}", transport);
            return BooleanAck.build(JMQCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        BooleanResponse checkResult = clusterManager.checkWritable(TopicName.parse(produceMessageCommitRequest.getTopic()), produceMessageCommitRequest.getApp(), connection.getHost());
        if (!checkResult.isSuccess()) {
            logger.warn("checkWritable failed, transport: {}, topic: {}, app: {}, code: {}", transport, produceMessageCommitRequest.getTopic(), produceMessageCommitRequest.getApp(), checkResult.getJmqCode());
            return new Command(new ProduceMessageCommitAck(CheckResultConverter.convertCommonCode(checkResult.getJmqCode())));
        }

        ProduceMessageCommitAck produceMessageCommitAck = produceMessageCommit(connection, produceMessageCommitRequest);
        return new Command(produceMessageCommitAck);
    }

    protected ProduceMessageCommitAck produceMessageCommit(Connection connection, ProduceMessageCommitRequest produceMessageCommitRequest) {
        Producer producer = new Producer(connection.getId(), produceMessageCommitRequest.getTopic(), produceMessageCommitRequest.getApp(), Producer.ProducerType.JMQ);

        BrokerCommit brokerCommit = new BrokerCommit();
        brokerCommit.setTopic(produceMessageCommitRequest.getTopic());
        brokerCommit.setApp(produceMessageCommitRequest.getApp());
        brokerCommit.setTxId(produceMessageCommitRequest.getTxId());

        try {
            produce.putTransactionMessage(producer, brokerCommit);
            return new ProduceMessageCommitAck(JMQCode.SUCCESS);
        } catch (JMQException e) {
            logger.error("produceMessage prepare exception, transport: {}, topic: {}, app: {}",
                    connection.getTransport().remoteAddress(), produceMessageCommitRequest.getTopic(), produceMessageCommitRequest.getApp(), e);
            return new ProduceMessageCommitAck(JMQCode.valueOf(e.getCode()));
        } catch (Exception e) {
            logger.error("produceMessage prepare exception, transport: {}, topic: {}, app: {}",
                    connection.getTransport().remoteAddress(), produceMessageCommitRequest.getTopic(), produceMessageCommitRequest.getApp(), e);
            return new ProduceMessageCommitAck(JMQCode.CN_UNKNOWN_ERROR);
        }
    }

    @Override
    public int type() {
        return JMQCommandType.PRODUCE_MESSAGE_COMMIT.getCode();
    }
}