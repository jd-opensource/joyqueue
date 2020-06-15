package com.jd.joyqueue.broker.jmq2.handler;

import com.jd.joyqueue.broker.jmq2.JMQ2CommandHandler;
import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;
import com.jd.joyqueue.broker.jmq2.JMQ2Consts;
import com.jd.joyqueue.broker.jmq2.command.AddProducer;
import com.jd.joyqueue.broker.jmq2.command.BooleanAck;
import com.jd.joyqueue.broker.jmq2.command.GetProducerHealth;
import com.jd.joyqueue.broker.jmq2.command.RemoveProducer;
import com.jd.joyqueue.broker.jmq2.helper.VersionHelper;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.BrokerContextAware;
import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.helper.SessionHelper;
import org.joyqueue.broker.monitor.SessionManager;
import org.joyqueue.domain.TopicConfig;
import org.joyqueue.domain.TopicName;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.network.session.Connection;
import org.joyqueue.network.session.ConnectionId;
import org.joyqueue.network.session.Producer;
import org.joyqueue.network.session.ProducerId;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.Types;
import org.joyqueue.network.transport.exception.TransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 生产者处理器
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/28
 */
public class ProducerHandler implements JMQ2CommandHandler, Types, BrokerContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(ProducerHandler.class);

    private ClusterManager clusterManager;
    private SessionManager sessionManager;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.clusterManager = brokerContext.getClusterManager();
        this.sessionManager = brokerContext.getSessionManager();
    }

    @Override
    public Command handle(Transport transport, Command command) throws TransportException {
        Object payload = command.getPayload();
        if (payload instanceof AddProducer) {
            return addProducer(transport, command, (AddProducer) payload);
        } else if (payload instanceof RemoveProducer) {
            return removeProducer(transport, command, (RemoveProducer) payload);
        } else if (payload instanceof GetProducerHealth) {
            return getProducerHealth(transport, command, (GetProducerHealth) payload);
        } else {
            throw new TransportException.RequestErrorException(JoyQueueCode.CN_COMMAND_UNSUPPORTED.getMessage(payload.getClass()));
        }
    }

    protected Command addProducer(Transport transport, Command command, AddProducer addProducer) {
        TopicName topic = addProducer.getTopic();
        ProducerId producerId = addProducer.getProducerId();
        ConnectionId connectionId = producerId.getConnectionId();
        Connection connection = sessionManager.getConnectionById(connectionId.getConnectionId());
        // 连接不存在
        if (connection == null) {
            logger.warn("connection {} is not exists, topic: {}", connectionId.getConnectionId(), topic);
            return BooleanAck.build(JoyQueueCode.FW_CONNECTION_NOT_EXISTS.getCode(),
                    String.format("connection %s is not exists. topic:%s", connectionId.getConnectionId(), topic));
        }

        // Broker被禁用了或不能发送消息
        if (clusterManager.tryGetProducer(topic, connection.getApp()) == null) {
            logger.warn("addProducer failed, transport: {}, topic: {}, app: {}", transport, topic, connection.getApp());
            return BooleanAck.build(JoyQueueCode.CN_NO_PERMISSION);
        }

        TopicConfig topicConfig = clusterManager.getTopicConfig(topic);
        if (topicConfig == null) {
            return BooleanAck.build(JoyQueueCode.CN_NO_PERMISSION);
        }

        // 确认版本号
        if (!VersionHelper.checkVersion(topicConfig, connection.getApp(), connection.getVersion())) {
            return BooleanAck.build(JoyQueueCode.CT_LOW_VERSION.getCode(),
                    String.format("current client version %s less than minVersion %s for using broadcast or sequential!", connection.getVersion(), JMQ2Consts.MIN_SUPPORTED_VERSION_STR));
        }

        Producer producer = new Producer(producerId.getProducerId(), connectionId.getConnectionId(), topic.getFullName());
        producer.setType(Producer.ProducerType.JMQ2);
        producer.setApp(connection.getApp());
        sessionManager.addProducer(producer);

        return BooleanAck.build();
    }

    protected Command removeProducer(Transport transport, Command command, RemoveProducer removeProducer) {
        ProducerId producerId = removeProducer.getProducerId();
        Producer producer = sessionManager.getProducerById(producerId.getProducerId());
        if (producer != null) {
            sessionManager.removeProducer(producer.getId());
        }
        return BooleanAck.build();
    }

    protected Command getProducerHealth(Transport transport, Command command, GetProducerHealth getProducerHealth) {
        Connection connection = SessionHelper.getConnection(transport);
        if (connection == null) {
            return BooleanAck.build(JoyQueueCode.CN_NO_PERMISSION);
        }
        if (!clusterManager.checkWritable(TopicName.parse(getProducerHealth.getTopic()), getProducerHealth.getApp(), null).isSuccess()) {
            return BooleanAck.build(JoyQueueCode.CN_NO_PERMISSION);
        }
        if (sessionManager.getProducerById(getProducerHealth.getProducerId()) == null) {
            return BooleanAck.build(JoyQueueCode.FW_PRODUCER_NOT_EXISTS);
        }
        return BooleanAck.build();
    }

    @Override
    public int[] types() {
        return new int[]{JMQ2CommandType.ADD_PRODUCER.getCode(), JMQ2CommandType.REMOVE_PRODUCER.getCode(), JMQ2CommandType.GET_PRODUCER_HEALTH.getCode()};
    }
}