package com.jd.journalq.broker.protocol.handler;

import com.google.common.collect.Lists;
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
import com.jd.journalq.network.command.BooleanAck;
import com.jd.journalq.network.command.FetchProduceFeedbackAckData;
import com.jd.journalq.network.command.FetchProduceFeedbackRequest;
import com.jd.journalq.network.command.FetchProduceFeedbackResponse;
import com.jd.journalq.network.command.JournalqCommandType;
import com.jd.journalq.network.session.Connection;
import com.jd.journalq.network.session.Producer;
import com.jd.journalq.network.session.TransactionId;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.response.BooleanResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * FetchProduceFeedbackRequestHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/19
 */
public class FetchProduceFeedbackRequestHandler implements JournalqCommandHandler, Type, BrokerContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(FetchProduceFeedbackRequestHandler.class);

    private Produce produce;
    private ClusterManager clusterManager;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.produce = brokerContext.getProduce();
        this.clusterManager = brokerContext.getClusterManager();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        FetchProduceFeedbackRequest fetchProduceFeedbackRequest = (FetchProduceFeedbackRequest) command.getPayload();
        Connection connection = SessionHelper.getConnection(transport);

        if (connection == null || !connection.isAuthorized(fetchProduceFeedbackRequest.getApp())) {
            logger.warn("connection is not exists, transport: {}", transport);
            return BooleanAck.build(JournalqCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        BooleanResponse checkResult = clusterManager.checkWritable(TopicName.parse(fetchProduceFeedbackRequest.getTopic()), fetchProduceFeedbackRequest.getApp(), connection.getHost());
        if (!checkResult.isSuccess()) {
            logger.warn("checkWritable failed, transport: {}, topic: {}, app: {}, code: {}", transport, fetchProduceFeedbackRequest.getTopic(), fetchProduceFeedbackRequest.getApp(), checkResult.getJournalqCode());
            return new Command(new FetchProduceFeedbackResponse(CheckResultConverter.convertCommonCode(checkResult.getJournalqCode())));
        }

        FetchProduceFeedbackResponse fetchProduceFeedbackResponse = FetchProduceFeedback(connection, fetchProduceFeedbackRequest);
        return new Command(fetchProduceFeedbackResponse);
    }

    protected FetchProduceFeedbackResponse FetchProduceFeedback(Connection connection, FetchProduceFeedbackRequest fetchProduceFeedbackRequest) {
        Producer producer = new Producer(connection.getId(), fetchProduceFeedbackRequest.getTopic(), fetchProduceFeedbackRequest.getApp(), Producer.ProducerType.JMQ);
        try {
            FetchProduceFeedbackResponse fetchProduceFeedbackResponse = new FetchProduceFeedbackResponse();
            List<TransactionId> transactionIdList = produce.getFeedback(producer, fetchProduceFeedbackRequest.getCount());
            fetchProduceFeedbackResponse.setData(buildFeedbackAckData(transactionIdList));
            fetchProduceFeedbackResponse.setCode(JournalqCode.SUCCESS);
            return fetchProduceFeedbackResponse;
        } catch (JournalqException e) {
            logger.error("fetch feedback exception, transport: {}, topic: {}, app: {}", connection.getTransport().remoteAddress(), fetchProduceFeedbackRequest.getTopic(), fetchProduceFeedbackRequest.getApp(), e);
            return new FetchProduceFeedbackResponse(JournalqCode.valueOf(e.getCode()));
        } catch (Exception e) {
            logger.error("fetch feedback exception, transport: {}, topic: {}, app: {}", connection.getTransport().remoteAddress(), fetchProduceFeedbackRequest.getTopic(), fetchProduceFeedbackRequest.getApp(), e);
            return new FetchProduceFeedbackResponse(JournalqCode.CN_UNKNOWN_ERROR);
        }
    }

    protected List<FetchProduceFeedbackAckData> buildFeedbackAckData(List<TransactionId> transactionIdList) {
        List<FetchProduceFeedbackAckData> result = Lists.newArrayListWithCapacity(transactionIdList.size());
        for (TransactionId transactionId : transactionIdList) {
            result.add(new FetchProduceFeedbackAckData(transactionId.getTopic(), transactionId.getTxId(), transactionId.getQueryId()));
        }
        return result;
    }

    @Override
    public int type() {
        return JournalqCommandType.FETCH_PRODUCE_FEEDBACK_REQUEST.getCode();
    }
}