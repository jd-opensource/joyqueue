package com.jd.journalq.broker.jmq.handler;

import com.google.common.collect.Maps;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.broker.consumer.Consume;
import com.jd.journalq.broker.consumer.model.PullResult;
import com.jd.journalq.broker.helper.SessionHelper;
import com.jd.journalq.broker.jmq.JMQCommandHandler;
import com.jd.journalq.broker.jmq.JMQContext;
import com.jd.journalq.broker.jmq.JMQContextAware;
import com.jd.journalq.broker.jmq.converter.CheckResultConverter;
import com.jd.journalq.broker.monitor.SessionManager;
import com.jd.journalq.broker.polling.LongPolling;
import com.jd.journalq.broker.polling.LongPollingManager;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.exception.JMQException;
import com.jd.journalq.network.command.BooleanAck;
import com.jd.journalq.network.command.FetchTopicMessageRequest;
import com.jd.journalq.network.command.FetchTopicMessageResponse;
import com.jd.journalq.network.command.FetchTopicMessageAckData;
import com.jd.journalq.network.command.FetchTopicMessageData;
import com.jd.journalq.network.command.JMQCommandType;
import com.jd.journalq.network.session.Connection;
import com.jd.journalq.network.session.Consumer;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.response.BooleanResponse;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

/**
 * FetchTopicMessageRequestHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/7
 */
public class FetchTopicMessageRequestHandler implements JMQCommandHandler, Type, JMQContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(FetchTopicMessageRequestHandler.class);

    private Consume consume;
    private SessionManager sessionManager;
    private ClusterManager clusterManager;
    private LongPollingManager longPollingManager;

    @Override
    public void setJmqContext(JMQContext jmqContext) {
        this.consume = jmqContext.getBrokerContext().getConsume();
        this.sessionManager = jmqContext.getBrokerContext().getSessionManager();
        this.clusterManager = jmqContext.getBrokerContext().getClusterManager();
        this.longPollingManager = jmqContext.getLongPollingManager();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        FetchTopicMessageRequest fetchTopicMessageRequest = (FetchTopicMessageRequest) command.getPayload();
        Connection connection = SessionHelper.getConnection(transport);

        if (connection == null || !connection.isAuthorized(fetchTopicMessageRequest.getApp())) {
            logger.warn("connection is not exists, transport: {}", transport);
            return BooleanAck.build(JMQCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        boolean isNeedLongPoll = fetchTopicMessageRequest.getTopics().size() == 1 && fetchTopicMessageRequest.getLongPollTimeout() > 0;
        Map<String, FetchTopicMessageAckData> result = Maps.newHashMapWithExpectedSize(fetchTopicMessageRequest.getTopics().size());

        for (Map.Entry<String, FetchTopicMessageData> entry : fetchTopicMessageRequest.getTopics().entrySet()) {
            String topic = entry.getKey();
            BooleanResponse checkResult = clusterManager.checkReadable(TopicName.parse(topic), fetchTopicMessageRequest.getApp(), connection.getHost());
            if (!checkResult.isSuccess()) {
                logger.warn("checkReadable failed, transport: {}, topic: {}, app: {}, code: {}", transport, topic, fetchTopicMessageRequest.getApp(), checkResult.getJmqCode());
                result.put(topic, new FetchTopicMessageAckData(CheckResultConverter.convertFetchCode(checkResult.getJmqCode())));
                continue;
            }

            String consumerId = connection.getConsumer(topic, fetchTopicMessageRequest.getApp());
            Consumer consumer = (StringUtils.isBlank(consumerId) ? null : sessionManager.getConsumerById(consumerId));

            if (consumer == null) {
                logger.warn("consumer is not exists, transport: {}", transport);
                result.put(topic, new FetchTopicMessageAckData(CheckResultConverter.convertFetchCode(JMQCode.FW_CONSUMER_NOT_EXISTS)));
                continue;
            }

            FetchTopicMessageData fetchTopicMessageData = entry.getValue();
            FetchTopicMessageAckData fetchTopicMessageAckData = fetchMessage(transport, consumer, fetchTopicMessageData.getCount(), fetchTopicMessageRequest.getAckTimeout());

            if (isNeedLongPoll && CollectionUtils.isEmpty(fetchTopicMessageAckData.getBuffers()) && clusterManager.isNeedLongPull(consumer.getTopic())) {
                if (longPollingManager.suspend(new LongPolling(consumer, fetchTopicMessageData.getCount(), fetchTopicMessageRequest.getAckTimeout(),
                        fetchTopicMessageRequest.getLongPollTimeout(), new FetchTopicMessageLongPollCallback(fetchTopicMessageRequest, command, transport)))) {
                    return null;
                }
            }

            result.put(topic, fetchTopicMessageAckData);
        }

        FetchTopicMessageResponse fetchTopicMessageResponse = new FetchTopicMessageResponse();
        fetchTopicMessageResponse.setData(result);
        return new Command(fetchTopicMessageResponse);
    }

    protected FetchTopicMessageAckData fetchMessage(Transport transport, Consumer consumer, int count, int ackTimeout) {
        FetchTopicMessageAckData fetchTopicMessageAckData = new FetchTopicMessageAckData();
        fetchTopicMessageAckData.setBuffers(Collections.emptyList());
        try {
            PullResult pullResult = consume.getMessage(consumer, count, ackTimeout);
            if (!pullResult.getJmqCode().equals(JMQCode.SUCCESS)) {
                logger.error("fetchTopicMessage exception, transport: {}, consumer: {}, count: {}", transport, consumer, count);
            }
            fetchTopicMessageAckData.setBuffers(pullResult.getBuffers());
            fetchTopicMessageAckData.setCode(pullResult.getJmqCode());
        } catch (JMQException e) {
            logger.error("fetchTopicMessage exception, transport: {}, consumer: {}, count: {}", transport, consumer, count, e);
            fetchTopicMessageAckData.setCode(JMQCode.valueOf(e.getCode()));
        } catch (Exception e) {
            logger.error("fetchTopicMessage exception, transport: {}, consumer: {}, count: {}", transport, consumer, count, e);
            fetchTopicMessageAckData.setCode(JMQCode.CN_UNKNOWN_ERROR);
        }
        return fetchTopicMessageAckData;
    }

    @Override
    public int type() {
        return JMQCommandType.FETCH_TOPIC_MESSAGE_REQUEST.getCode();
    }
}