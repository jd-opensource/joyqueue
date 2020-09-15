package org.joyqueue.broker.joyqueue0.handler;

import org.joyqueue.broker.joyqueue0.Joyqueue0CommandHandler;
import org.joyqueue.broker.joyqueue0.Joyqueue0CommandType;
import org.joyqueue.broker.joyqueue0.command.BooleanAck;
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
public class AuthorizeHandler implements Joyqueue0CommandHandler, Type, BrokerContextAware {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private Authentication authentication;
    @Override
    public int type() {
        return Joyqueue0CommandType.AUTHORIZATION.getCode();
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
