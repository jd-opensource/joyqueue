package com.jd.joyqueue.broker.jmq2.handler;

import com.jd.joyqueue.broker.jmq2.JMQ2CommandHandler;
import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;
import com.jd.joyqueue.broker.jmq2.command.ClientProfileAck;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.network.transport.exception.TransportException;

/**
 * clientProfile处理
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/29
 */
public class ClientProfileHandler implements JMQ2CommandHandler, Type {

    @Override
    public Command handle(Transport transport, Command command) throws TransportException {
        ClientProfileAck clientProfileAck = new ClientProfileAck();
        return new Command(clientProfileAck);
    }

    @Override
    public int type() {
        return JMQ2CommandType.CLIENT_PROFILE.getCode();
    }
}