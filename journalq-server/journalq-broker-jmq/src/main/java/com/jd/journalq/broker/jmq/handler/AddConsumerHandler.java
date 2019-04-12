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
package com.jd.journalq.broker.jmq.handler;

import com.google.common.collect.Maps;
import com.jd.journalq.broker.jmq.JMQCommandHandler;
import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.BrokerContextAware;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.broker.helper.SessionHelper;
import com.jd.journalq.broker.monitor.SessionManager;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.network.command.AddConsumer;
import com.jd.journalq.network.command.AddConsumerAck;
import com.jd.journalq.network.command.BooleanAck;
import com.jd.journalq.network.command.JMQCommandType;
import com.jd.journalq.network.session.Connection;
import com.jd.journalq.network.session.Consumer;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.response.BooleanResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * AddConsumerHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/10
 */
public class AddConsumerHandler implements JMQCommandHandler, Type, BrokerContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(AddConsumerHandler.class);

    private SessionManager sessionManager;
    private ClusterManager clusterManager;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.sessionManager = brokerContext.getSessionManager();
        this.clusterManager = brokerContext.getClusterManager();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        AddConsumer addConsumer = (AddConsumer) command.getPayload();
        Connection connection = SessionHelper.getConnection(transport);

        if (connection == null || !connection.isAuthorized(addConsumer.getApp())) {
            logger.warn("connection is not exists, transport: {}", transport);
            return BooleanAck.build(JMQCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        Map<String, String> result = Maps.newHashMap();

        for (String topic : addConsumer.getTopics()) {
            TopicName topicName = TopicName.parse(topic);

            BooleanResponse checkResult = clusterManager.checkReadable(topicName, addConsumer.getApp(), null);
            if (!checkResult.isSuccess()) {
                logger.warn("checkReadable failed, transport: {}, topic: {}, app: {}, code: {}", transport, topicName, addConsumer.getApp(), checkResult.getJmqCode());
                return BooleanAck.build(JMQCode.CN_NO_PERMISSION);
            }

            Consumer consumer = buildConsumer(connection, topic, addConsumer.getApp(), addConsumer.getSequence());
            sessionManager.addConsumer(consumer);
            result.put(topic, consumer.getId());
        }

        AddConsumerAck addConsumerAck = new AddConsumerAck();
        addConsumerAck.setConsumerIds(result);
        return new Command(addConsumerAck);
    }

    protected Consumer buildConsumer(Connection connection, String topic, String app, long sequence) {
        Consumer consumer = new Consumer();
        consumer.setId(generateConsumerId(connection, topic, app, sequence));
        consumer.setConnectionId(connection.getId());
        consumer.setApp(app);
        consumer.setTopic(topic);
        consumer.setType(Consumer.ConsumeType.JMQ);
        return consumer;
    }

    protected String generateConsumerId(Connection connection, String topic, String app, long sequence) {
        return String.format("%s_%s_consumer_%s_%s", connection.getId(), sequence, app, topic);
    }

    @Override
    public int type() {
        return JMQCommandType.ADD_CONSUMER.getCode();
    }
}