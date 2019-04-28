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
import com.jd.journalq.broker.JournalqCommandHandler;
import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.BrokerContextAware;
import com.jd.journalq.broker.cluster.ClusterManager;
import com.jd.journalq.broker.helper.SessionHelper;
import com.jd.journalq.broker.monitor.SessionManager;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.exception.JournalqCode;
import com.jd.journalq.network.command.AddProducer;
import com.jd.journalq.network.command.AddProducerAck;
import com.jd.journalq.network.command.BooleanAck;
import com.jd.journalq.network.command.JournalqCommandType;
import com.jd.journalq.network.session.Connection;
import com.jd.journalq.network.session.Producer;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.response.BooleanResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * AddProducerHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/10
 */
public class AddProducerHandler implements JournalqCommandHandler, Type, BrokerContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(AddProducerHandler.class);

    private SessionManager sessionManager;
    private ClusterManager clusterManager;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.sessionManager = brokerContext.getSessionManager();
        this.clusterManager = brokerContext.getClusterManager();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        AddProducer addProducer = (AddProducer) command.getPayload();
        Connection connection = SessionHelper.getConnection(transport);

        if (connection == null || !connection.isAuthorized(addProducer.getApp())) {
            logger.warn("connection is not exists, transport: {}", transport);
            return BooleanAck.build(JournalqCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        Map<String, String> result = Maps.newHashMap();

        for (String topic : addProducer.getTopics()) {
            TopicName topicName = TopicName.parse(topic);

            BooleanResponse checkResult = clusterManager.checkWritable(topicName, addProducer.getApp(), null);
            if (!checkResult.isSuccess()) {
                logger.warn("checkWritable failed, transport: {}, topic: {}, app: {}, code: {}", transport, topicName, addProducer.getApp(), checkResult.getJournalqCode());
                return BooleanAck.build(JournalqCode.CN_NO_PERMISSION);
            }

            Producer producer = buildProducer(connection, topic, addProducer.getApp(), addProducer.getSequence());
            sessionManager.addProducer(producer);
            result.put(topic, producer.getId());
        }

        AddProducerAck addProducerAck = new AddProducerAck();
        addProducerAck.setProducerIds(result);
        return new Command(addProducerAck);
    }

    protected Producer buildProducer(Connection connection, String topic, String app, long sequence) {
        Producer producer = new Producer();
        producer.setId(generateProducerId(connection, topic, app, sequence));
        producer.setConnectionId(connection.getId());
        producer.setApp(app);
        producer.setTopic(topic);
        producer.setType(Producer.ProducerType.JMQ);
        return producer;
    }

    protected String generateProducerId(Connection connection, String topic, String app, long sequence) {
        return String.format("%s_%s_producer_%s_%s", connection.getId(), sequence, app, topic);
    }

    @Override
    public int type() {
        return JournalqCommandType.ADD_PRODUCER.getCode();
    }
}