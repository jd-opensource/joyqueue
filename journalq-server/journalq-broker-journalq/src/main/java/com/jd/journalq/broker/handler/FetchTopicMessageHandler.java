/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jd.journalq.broker.handler;

import com.google.common.collect.Maps;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.broker.consumer.Consume;
import com.jd.journalq.broker.consumer.model.PullResult;
import com.jd.journalq.broker.helper.SessionHelper;
import com.jd.journalq.broker.JournalqCommandHandler;
import com.jd.journalq.broker.JournalqContext;
import com.jd.journalq.broker.JournalqContextAware;
import com.jd.journalq.broker.converter.CheckResultConverter;
import com.jd.journalq.broker.monitor.SessionManager;
import com.jd.journalq.broker.polling.LongPolling;
import com.jd.journalq.broker.polling.LongPollingManager;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.exception.JournalqCode;
import com.jd.journalq.exception.JournalqException;
import com.jd.journalq.network.command.BooleanAck;
import com.jd.journalq.network.command.FetchTopicMessage;
import com.jd.journalq.network.command.FetchTopicMessageAck;
import com.jd.journalq.network.command.FetchTopicMessageAckData;
import com.jd.journalq.network.command.FetchTopicMessageData;
import com.jd.journalq.network.command.JournalqCommandType;
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
 * FetchTopicMessageHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/7
 */
public class FetchTopicMessageHandler implements JournalqCommandHandler, Type, JournalqContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(FetchTopicMessageHandler.class);

    private Consume consume;
    private SessionManager sessionManager;
    private ClusterManager clusterManager;
    private LongPollingManager longPollingManager;

    @Override
    public void setJmqContext(JournalqContext journalqContext) {
        this.consume = journalqContext.getBrokerContext().getConsume();
        this.sessionManager = journalqContext.getBrokerContext().getSessionManager();
        this.clusterManager = journalqContext.getBrokerContext().getClusterManager();
        this.longPollingManager = journalqContext.getLongPollingManager();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        FetchTopicMessage fetchTopicMessage = (FetchTopicMessage) command.getPayload();
        Connection connection = SessionHelper.getConnection(transport);

        if (connection == null || !connection.isAuthorized(fetchTopicMessage.getApp())) {
            logger.warn("connection is not exists, transport: {}", transport);
            return BooleanAck.build(JournalqCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        boolean isNeedLongPoll = fetchTopicMessage.getTopics().size() == 1 && fetchTopicMessage.getLongPollTimeout() > 0;
        Map<String, FetchTopicMessageAckData> result = Maps.newHashMapWithExpectedSize(fetchTopicMessage.getTopics().size());

        for (Map.Entry<String, FetchTopicMessageData> entry : fetchTopicMessage.getTopics().entrySet()) {
            String topic = entry.getKey();
            BooleanResponse checkResult = clusterManager.checkReadable(TopicName.parse(topic), fetchTopicMessage.getApp(), connection.getHost());
            if (!checkResult.isSuccess()) {
                logger.warn("checkReadable failed, transport: {}, topic: {}, app: {}, code: {}", transport, topic, fetchTopicMessage.getApp(), checkResult.getJournalqCode());
                result.put(topic, new FetchTopicMessageAckData(CheckResultConverter.convertFetchCode(checkResult.getJournalqCode())));
                continue;
            }

            String consumerId = connection.getConsumer(topic, fetchTopicMessage.getApp());
            Consumer consumer = (StringUtils.isBlank(consumerId) ? null : sessionManager.getConsumerById(consumerId));

            if (consumer == null) {
                logger.warn("consumer is not exists, transport: {}", transport);
                result.put(topic, new FetchTopicMessageAckData(CheckResultConverter.convertFetchCode(JournalqCode.FW_CONSUMER_NOT_EXISTS)));
                continue;
            }

            FetchTopicMessageData fetchTopicMessageData = entry.getValue();
            FetchTopicMessageAckData fetchTopicMessageAckData = fetchMessage(transport, consumer, fetchTopicMessageData.getCount(), fetchTopicMessage.getAckTimeout());

            if (isNeedLongPoll && CollectionUtils.isEmpty(fetchTopicMessageAckData.getBuffers()) && clusterManager.isNeedLongPull(consumer.getTopic())) {
                if (longPollingManager.suspend(new LongPolling(consumer, fetchTopicMessageData.getCount(), fetchTopicMessage.getAckTimeout(),
                        fetchTopicMessage.getLongPollTimeout(), new FetchTopicMessageLongPollCallback(fetchTopicMessage, command, transport)))) {
                    return null;
                }
            }

            result.put(topic, fetchTopicMessageAckData);
        }

        FetchTopicMessageAck fetchTopicMessageAck = new FetchTopicMessageAck();
        fetchTopicMessageAck.setData(result);
        return new Command(fetchTopicMessageAck);
    }

    protected FetchTopicMessageAckData fetchMessage(Transport transport, Consumer consumer, int count, int ackTimeout) {
        FetchTopicMessageAckData fetchTopicMessageAckData = new FetchTopicMessageAckData();
        fetchTopicMessageAckData.setBuffers(Collections.emptyList());
        try {
            PullResult pullResult = consume.getMessage(consumer, count, ackTimeout);
            if (!pullResult.getJournalqCode().equals(JournalqCode.SUCCESS)) {
                logger.error("fetchTopicMessage exception, transport: {}, consumer: {}, count: {}", transport, consumer, count);
            }
            fetchTopicMessageAckData.setBuffers(pullResult.getBuffers());
            fetchTopicMessageAckData.setCode(pullResult.getJournalqCode());
        } catch (JournalqException e) {
            logger.error("fetchTopicMessage exception, transport: {}, consumer: {}, count: {}", transport, consumer, count, e);
            fetchTopicMessageAckData.setCode(JournalqCode.valueOf(e.getCode()));
        } catch (Exception e) {
            logger.error("fetchTopicMessage exception, transport: {}, consumer: {}, count: {}", transport, consumer, count, e);
            fetchTopicMessageAckData.setCode(JournalqCode.CN_UNKNOWN_ERROR);
        }
        return fetchTopicMessageAckData;
    }

    @Override
    public int type() {
        return JournalqCommandType.FETCH_TOPIC_MESSAGE.getCode();
    }
}