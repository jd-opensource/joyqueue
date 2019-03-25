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
import com.jd.journalq.network.command.AddProducer;
import com.jd.journalq.network.command.AddProducerAck;
import com.jd.journalq.network.command.BooleanAck;
import com.jd.journalq.network.command.JMQCommandType;
import com.jd.journalq.network.session.Connection;
import com.jd.journalq.network.session.Producer;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * AddProducerHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/10
 */
public class AddProducerHandler implements JMQCommandHandler, Type, BrokerContextAware {

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
            return BooleanAck.build(JMQCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        Map<String, String> result = Maps.newHashMap();

        for (String topic : addProducer.getTopics()) {
            TopicName topicName = TopicName.parse(topic);

            if (!clusterManager.checkWritable(topicName, addProducer.getApp(), null).isSuccess()) {
                logger.warn("checkWritable failed, transport: {}, topic: {}, app: {}", transport, topicName, addProducer.getApp());
                return BooleanAck.build(JMQCode.CN_NO_PERMISSION);
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
        return JMQCommandType.ADD_PRODUCER.getCode();
    }
}