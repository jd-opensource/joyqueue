/**
 * Copyright 2018 The JoyQueue Authors.
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
import io.chubao.joyqueue.broker.BrokerContext;
import io.chubao.joyqueue.broker.BrokerContextAware;
import io.chubao.joyqueue.broker.cluster.ClusterManager;
import io.chubao.joyqueue.broker.helper.SessionHelper;
import io.chubao.joyqueue.broker.monitor.SessionManager;
import io.chubao.joyqueue.broker.protocol.JoyQueueCommandHandler;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.network.command.AddConsumerRequest;
import io.chubao.joyqueue.network.command.AddConsumerResponse;
import io.chubao.joyqueue.network.command.BooleanAck;
import io.chubao.joyqueue.network.command.JoyQueueCommandType;
import io.chubao.joyqueue.network.session.Connection;
import io.chubao.joyqueue.network.session.Consumer;
import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * AddConsumerRequestHandler
 *
 * author: gaohaoxiang
 * date: 2018/12/10
 */
public class AddConsumerRequestHandler implements JoyQueueCommandHandler, Type, BrokerContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(AddConsumerRequestHandler.class);

    private SessionManager sessionManager;
    private ClusterManager clusterManager;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.sessionManager = brokerContext.getSessionManager();
        this.clusterManager = brokerContext.getClusterManager();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        AddConsumerRequest addConsumerRequest = (AddConsumerRequest) command.getPayload();
        Connection connection = SessionHelper.getConnection(transport);

        if (connection == null || !connection.isAuthorized(addConsumerRequest.getApp())) {
            logger.warn("connection is not exists, transport: {}", transport);
            return BooleanAck.build(JoyQueueCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        Map<String, String> result = Maps.newHashMap();

        for (String topic : addConsumerRequest.getTopics()) {
            TopicName topicName = TopicName.parse(topic);

            if (clusterManager.tryGetConsumerPolicy(topicName, addConsumerRequest.getApp()) == null) {
                logger.warn("addConsumer failed, transport: {}, topic: {}, app: {}", transport, topicName, addConsumerRequest.getApp());
                return BooleanAck.build(JoyQueueCode.CN_NO_PERMISSION);
            }

            Consumer consumer = buildConsumer(connection, topic, addConsumerRequest.getApp(), addConsumerRequest.getSequence());
            sessionManager.addConsumer(consumer);
            result.put(topic, consumer.getId());
        }

        AddConsumerResponse addConsumerResponse = new AddConsumerResponse();
        addConsumerResponse.setConsumerIds(result);
        return new Command(addConsumerResponse);
    }

    protected Consumer buildConsumer(Connection connection, String topic, String app, long sequence) {
        Consumer consumer = new Consumer();
        consumer.setId(generateConsumerId(connection, topic, app, sequence));
        consumer.setConnectionId(connection.getId());
        consumer.setApp(app);
        consumer.setTopic(topic);
        consumer.setType(Consumer.ConsumeType.JOYQUEUE);
        return consumer;
    }

    protected String generateConsumerId(Connection connection, String topic, String app, long sequence) {
        return String.format("%s_%s_consumer_%s_%s", connection.getId(), sequence, app, topic);
    }

    @Override
    public int type() {
        return JoyQueueCommandType.ADD_CONSUMER_REQUEST.getCode();
    }
}