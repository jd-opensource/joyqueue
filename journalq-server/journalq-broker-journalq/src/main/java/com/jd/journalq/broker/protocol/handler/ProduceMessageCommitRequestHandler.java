package com.jd.journalq.broker.protocol.handler;

import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.BrokerContextAware;
import com.jd.journalq.broker.protocol.JournalqCommandHandler;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.broker.protocol.converter.CheckResultConverter;
import com.jd.journalq.broker.helper.SessionHelper;
import com.jd.journalq.broker.producer.Produce;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.exception.JournalqCode;
import com.jd.journalq.exception.JournalqException;
import com.jd.journalq.message.BrokerCommit;
import com.jd.journalq.network.command.BooleanAck;
import com.jd.journalq.network.command.JournalqCommandType;
import com.jd.journalq.network.command.ProduceMessageCommitRequest;
import com.jd.journalq.network.command.ProduceMessageCommitResponse;
import com.jd.journalq.network.session.Connection;
import com.jd.journalq.network.session.Producer;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.response.BooleanResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ProduceMessageCommitRequestHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/19
 */
public class ProduceMessageCommitRequestHandler implements JournalqCommandHandler, Type, BrokerContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(ProduceMessageCommitRequestHandler.class);

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
            return BooleanAck.build(JournalqCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        BooleanResponse checkResult = clusterManager.checkWritable(TopicName.parse(produceMessageCommitRequest.getTopic()), produceMessageCommitRequest.getApp(), connection.getHost());
        if (!checkResult.isSuccess()) {
            logger.warn("checkWritable failed, transport: {}, topic: {}, app: {}, code: {}", transport, produceMessageCommitRequest.getTopic(), produceMessageCommitRequest.getApp(), checkResult.getJournalqCode());
            return new Command(new ProduceMessageCommitResponse(CheckResultConverter.convertCommonCode(checkResult.getJournalqCode())));
        }

        ProduceMessageCommitResponse produceMessageCommitResponse = produceMessageCommit(connection, produceMessageCommitRequest);
        return new Command(produceMessageCommitResponse);
    }

    protected ProduceMessageCommitResponse produceMessageCommit(Connection connection, ProduceMessageCommitRequest produceMessageCommitRequest) {
        Producer producer = new Producer(connection.getId(), produceMessageCommitRequest.getTopic(), produceMessageCommitRequest.getApp(), Producer.ProducerType.JMQ);

        BrokerCommit brokerCommit = new BrokerCommit();
        brokerCommit.setTopic(produceMessageCommitRequest.getTopic());
        brokerCommit.setApp(produceMessageCommitRequest.getApp());
        brokerCommit.setTxId(produceMessageCommitRequest.getTxId());

        try {
            produce.putTransactionMessage(producer, brokerCommit);
            return new ProduceMessageCommitResponse(JournalqCode.SUCCESS);
        } catch (JournalqException e) {
            logger.error("produceMessage commit exception, transport: {}, topic: {}, app: {}",
                    connection.getTransport().remoteAddress(), produceMessageCommitRequest.getTopic(), produceMessageCommitRequest.getApp(), e);
            return new ProduceMessageCommitResponse(JournalqCode.valueOf(e.getCode()));
        } catch (Exception e) {
            logger.error("produceMessage commit exception, transport: {}, topic: {}, app: {}",
                    connection.getTransport().remoteAddress(), produceMessageCommitRequest.getTopic(), produceMessageCommitRequest.getApp(), e);
            return new ProduceMessageCommitResponse(JournalqCode.CN_UNKNOWN_ERROR);
        }
    }

    @Override
    public int type() {
        return JournalqCommandType.PRODUCE_MESSAGE_COMMIT_REQUEST.getCode();
    }
}