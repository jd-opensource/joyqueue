/**
 * Copyright 2019 The JoyQueue Authors.
 *
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
package io.chubao.joyqueue.broker.protocol.handler;

import com.google.common.collect.Maps;
import io.chubao.joyqueue.broker.cluster.ClusterManager;
import io.chubao.joyqueue.broker.consumer.Consume;
import io.chubao.joyqueue.broker.consumer.model.PullResult;
import io.chubao.joyqueue.broker.helper.SessionHelper;
import io.chubao.joyqueue.broker.monitor.SessionManager;
import io.chubao.joyqueue.broker.network.traffic.Traffic;
import io.chubao.joyqueue.broker.polling.LongPolling;
import io.chubao.joyqueue.broker.polling.LongPollingManager;
import io.chubao.joyqueue.broker.protocol.JoyQueueCommandHandler;
import io.chubao.joyqueue.broker.protocol.JoyQueueContext;
import io.chubao.joyqueue.broker.protocol.JoyQueueContextAware;
import io.chubao.joyqueue.broker.protocol.command.FetchTopicMessageResponse;
import io.chubao.joyqueue.broker.protocol.converter.CheckResultConverter;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.exception.JoyQueueException;
import io.chubao.joyqueue.network.command.BooleanAck;
import io.chubao.joyqueue.network.command.FetchTopicMessageAckData;
import io.chubao.joyqueue.network.command.FetchTopicMessageData;
import io.chubao.joyqueue.network.command.FetchTopicMessageRequest;
import io.chubao.joyqueue.network.command.JoyQueueCommandType;
import io.chubao.joyqueue.network.session.Connection;
import io.chubao.joyqueue.network.session.Consumer;
import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.response.BooleanResponse;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

/**
 * FetchTopicMessageRequestHandler
 *
 * author: gaohaoxiang
 * date: 2018/12/7
 */
public class FetchTopicMessageRequestHandler implements JoyQueueCommandHandler, Type, JoyQueueContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(FetchTopicMessageRequestHandler.class);

    private Consume consume;
    private SessionManager sessionManager;
    private ClusterManager clusterManager;
    private LongPollingManager longPollingManager;

    @Override
    public void setJoyQueueContext(JoyQueueContext joyQueueContext) {
        this.consume = joyQueueContext.getBrokerContext().getConsume();
        this.sessionManager = joyQueueContext.getBrokerContext().getSessionManager();
        this.clusterManager = joyQueueContext.getBrokerContext().getClusterManager();
        this.longPollingManager = joyQueueContext.getLongPollingManager();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        FetchTopicMessageRequest fetchTopicMessageRequest = (FetchTopicMessageRequest) command.getPayload();
        Connection connection = SessionHelper.getConnection(transport);

        if (connection == null || !connection.isAuthorized(fetchTopicMessageRequest.getApp())) {
            logger.warn("connection is not exists, transport: {}, app: {}", transport, fetchTopicMessageRequest.getApp());
            return BooleanAck.build(JoyQueueCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        boolean isNeedLongPoll = fetchTopicMessageRequest.getTopics().size() == 1 && fetchTopicMessageRequest.getLongPollTimeout() > 0;
        Map<String, FetchTopicMessageAckData> result = Maps.newHashMapWithExpectedSize(fetchTopicMessageRequest.getTopics().size());
        Traffic traffic = new Traffic(fetchTopicMessageRequest.getApp());

        for (Map.Entry<String, FetchTopicMessageData> entry : fetchTopicMessageRequest.getTopics().entrySet()) {
            String topic = entry.getKey();
            BooleanResponse checkResult = clusterManager.checkReadable(TopicName.parse(topic), fetchTopicMessageRequest.getApp(), connection.getHost());
            if (!checkResult.isSuccess()) {
                logger.warn("checkReadable failed, transport: {}, topic: {}, app: {}, code: {}", transport, topic, fetchTopicMessageRequest.getApp(), checkResult.getJoyQueueCode());
                result.put(topic, new FetchTopicMessageAckData(CheckResultConverter.convertFetchCode(command.getHeader().getVersion(), checkResult.getJoyQueueCode())));
                traffic.record(topic, 0);
                continue;
            }

            String consumerId = connection.getConsumer(topic, fetchTopicMessageRequest.getApp());
            Consumer consumer = (StringUtils.isBlank(consumerId) ? null : sessionManager.getConsumerById(consumerId));

            if (consumer == null) {
                logger.warn("consumer is not exists, transport: {}", transport);
                result.put(topic, new FetchTopicMessageAckData(CheckResultConverter.convertFetchCode(command.getHeader().getVersion(), JoyQueueCode.FW_CONSUMER_NOT_EXISTS)));
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

            traffic.record(topic, fetchTopicMessageAckData.getSize());
            result.put(topic, fetchTopicMessageAckData);
        }

        FetchTopicMessageResponse fetchTopicMessageResponse = new FetchTopicMessageResponse();
        fetchTopicMessageResponse.setTraffic(traffic);
        fetchTopicMessageResponse.setData(result);
        return new Command(fetchTopicMessageResponse);
    }

    protected FetchTopicMessageAckData fetchMessage(Transport transport, Consumer consumer, int count, int ackTimeout) {
        FetchTopicMessageAckData fetchTopicMessageAckData = new FetchTopicMessageAckData();
        fetchTopicMessageAckData.setBuffers(Collections.emptyList());
        try {
            PullResult pullResult = consume.getMessage(consumer, count, ackTimeout);
            if (!pullResult.getCode().equals(JoyQueueCode.SUCCESS)) {
                logger.error("fetchTopicMessage exception, transport: {}, consumer: {}, count: {}", transport, consumer, count);
            }
            fetchTopicMessageAckData.setBuffers(pullResult.getBuffers());
            fetchTopicMessageAckData.setCode(pullResult.getCode());
        } catch (JoyQueueException e) {
            logger.error("fetchTopicMessage exception, transport: {}, consumer: {}, count: {}", transport, consumer, count, e);
            fetchTopicMessageAckData.setCode(JoyQueueCode.valueOf(e.getCode()));
        } catch (Exception e) {
            logger.error("fetchTopicMessage exception, transport: {}, consumer: {}, count: {}", transport, consumer, count, e);
            fetchTopicMessageAckData.setCode(JoyQueueCode.CN_UNKNOWN_ERROR);
        }
        return fetchTopicMessageAckData;
    }

    @Override
    public int type() {
        return JoyQueueCommandType.FETCH_TOPIC_MESSAGE_REQUEST.getCode();
    }
}