package com.jd.journalq.broker.jmq.handler;

import com.jd.journalq.broker.jmq.JMQCommandHandler;
import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.BrokerContextAware;
import com.jd.journalq.broker.helper.SessionHelper;
import com.jd.journalq.broker.monitor.SessionManager;
import com.jd.journalq.common.exception.JMQCode;
import com.jd.journalq.common.network.command.BooleanAck;
import com.jd.journalq.common.network.command.JMQCommandType;
import com.jd.journalq.common.network.command.RemoveConsumer;
import com.jd.journalq.common.network.session.Connection;
import com.jd.journalq.common.network.transport.Transport;
import com.jd.journalq.common.network.transport.command.Command;
import com.jd.journalq.common.network.transport.command.Type;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RemoveConsumerHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/28
 */
public class RemoveConsumerHandler implements JMQCommandHandler, Type, BrokerContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(RemoveConsumerHandler.class);

    private SessionManager sessionManager;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.sessionManager = brokerContext.getSessionManager();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        RemoveConsumer removeConsumer = (RemoveConsumer) command.getPayload();
        Connection connection = SessionHelper.getConnection(transport);

        if (connection == null || !connection.isAuthorized(removeConsumer.getApp())) {
            logger.warn("connection is not exists, transport: {}", transport);
            return BooleanAck.build(JMQCode.FW_CONNECTION_NOT_EXISTS.getCode());
        }

        for (String topic : removeConsumer.getTopics()) {
            String consumerId = connection.getConsumer(topic, removeConsumer.getApp());
            if (StringUtils.isBlank(consumerId)) {
                continue;
            }
            sessionManager.removeConsumer(consumerId);
        }

        return BooleanAck.build();
    }

    @Override
    public int type() {
        return JMQCommandType.REMOVE_CONSUMER.getCode();
    }
}