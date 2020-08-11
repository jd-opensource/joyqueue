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
package org.joyqueue.broker.protocol.handler;

import com.google.common.collect.Maps;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.BrokerContextAware;
import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.helper.SessionHelper;
import org.joyqueue.broker.monitor.SessionManager;
import org.joyqueue.broker.protocol.JoyQueueCommandHandler;
import org.joyqueue.domain.TopicName;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.network.command.AddConsumerRequest;
import org.joyqueue.network.command.AddConsumerResponse;
import org.joyqueue.network.command.BooleanAck;
import org.joyqueue.network.command.JoyQueueCommandType;
import org.joyqueue.network.session.Connection;
import org.joyqueue.network.session.Consumer;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.Type;
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
            logger.warn("connection does not exist, transport: {}", transport);
            return BooleanAck.build(JoyQueueCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        Map<String, String> result = Maps.newHashMap();

        for (String topic : addConsumerRequest.getTopics()) {
            if (connection.containsConsumer(topic, addConsumerRequest.getApp())) {
                result.put(topic, connection.getConsumer(topic, addConsumerRequest.getApp()));
                continue;
            }

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
