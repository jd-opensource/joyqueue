package com.jd.joyqueue.broker.jmq2.handler;

import com.jd.joyqueue.broker.jmq2.JMQ2CommandHandler;
import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;
import com.jd.joyqueue.broker.jmq2.command.BooleanAck;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.BrokerContextAware;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.network.command.Authorization;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.network.transport.exception.TransportException;
import org.joyqueue.security.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author wylixiaobin
 * Date: 2018/10/19
 */
@Deprecated
public class AuthorizeHandler implements JMQ2CommandHandler, Type, BrokerContextAware {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private Authentication authentication;
    @Override
    public int type() {
        return JMQ2CommandType.AUTHORIZATION.getCode();
    }

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.authentication = brokerContext.getAuthentication();
    }

    @Override
    public Command handle(Transport transport, Command command) throws TransportException {
        Authorization authorization = (Authorization) command.getPayload();
        return authentication.auth(authorization.getApp(),authorization.getToken()).isSuccess()?BooleanAck.build():BooleanAck.build(JoyQueueCode.CN_AUTHENTICATION_ERROR);
    }
}
