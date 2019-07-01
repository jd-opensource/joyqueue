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
package com.jd.joyqueue.broker.protocol.handler;

import com.google.common.collect.Maps;
import com.jd.joyqueue.broker.BrokerContext;
import com.jd.joyqueue.broker.BrokerContextAware;
import com.jd.joyqueue.broker.cluster.ClusterManager;
import com.jd.joyqueue.broker.helper.SessionHelper;
import com.jd.joyqueue.broker.monitor.SessionManager;
import com.jd.joyqueue.broker.protocol.JoyQueueCommandHandler;
import com.jd.joyqueue.domain.TopicName;
import com.jd.joyqueue.exception.JoyQueueCode;
import com.jd.joyqueue.network.command.AddProducerRequest;
import com.jd.joyqueue.network.command.AddProducerResponse;
import com.jd.joyqueue.network.command.BooleanAck;
import com.jd.joyqueue.network.command.JoyQueueCommandType;
import com.jd.joyqueue.network.session.Connection;
import com.jd.joyqueue.network.session.Producer;
import com.jd.joyqueue.network.transport.Transport;
import com.jd.joyqueue.network.transport.command.Command;
import com.jd.joyqueue.network.transport.command.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * AddProducerRequestHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/10
 */
public class AddProducerRequestHandler implements JoyQueueCommandHandler, Type, BrokerContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(AddProducerRequestHandler.class);

    private SessionManager sessionManager;
    private ClusterManager clusterManager;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.sessionManager = brokerContext.getSessionManager();
        this.clusterManager = brokerContext.getClusterManager();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        AddProducerRequest addProducerRequest = (AddProducerRequest) command.getPayload();
        Connection connection = SessionHelper.getConnection(transport);

        if (connection == null || !connection.isAuthorized(addProducerRequest.getApp())) {
            logger.warn("connection is not exists, transport: {}, app: {}", transport, addProducerRequest.getApp());
            return BooleanAck.build(JoyQueueCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        Map<String, String> result = Maps.newHashMap();

        for (String topic : addProducerRequest.getTopics()) {
            TopicName topicName = TopicName.parse(topic);

            if (clusterManager.tryGetProducer(topicName, addProducerRequest.getApp()) == null) {
                logger.warn("addProducer failed, transport: {}, topic: {}, app: {}, code: {}", transport, topicName, addProducerRequest.getApp());
                return BooleanAck.build(JoyQueueCode.CN_NO_PERMISSION);
            }

            Producer producer = buildProducer(connection, topic, addProducerRequest.getApp(), addProducerRequest.getSequence());
            sessionManager.addProducer(producer);
            result.put(topic, producer.getId());
        }

        AddProducerResponse addProducerResponse = new AddProducerResponse();
        addProducerResponse.setProducerIds(result);
        return new Command(addProducerResponse);
    }

    protected Producer buildProducer(Connection connection, String topic, String app, long sequence) {
        Producer producer = new Producer();
        producer.setId(generateProducerId(connection, topic, app, sequence));
        producer.setConnectionId(connection.getId());
        producer.setApp(app);
        producer.setTopic(topic);
        producer.setType(Producer.ProducerType.JOYQUEUE);
        return producer;
    }

    protected String generateProducerId(Connection connection, String topic, String app, long sequence) {
        return String.format("%s_%s_producer_%s_%s", connection.getId(), sequence, app, topic);
    }

    @Override
    public int type() {
        return JoyQueueCommandType.ADD_PRODUCER_REQUEST.getCode();
    }
}