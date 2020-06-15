package com.jd.joyqueue.broker.jmq2.handler;

import com.jd.joyqueue.broker.jmq2.JMQ2CommandHandler;
import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;
import com.jd.joyqueue.broker.jmq2.command.BooleanAck;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.network.transport.exception.TransportException;

/**
 * 心跳处理
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/29
 */
public class HeartbeatHandler implements JMQ2CommandHandler, Type {

    @Override
    public Command handle(Transport transport, Command command) throws TransportException {
        return BooleanAck.build();
    }

    @Override
    public int type() {
        return JMQ2CommandType.HEARTBEAT.getCode();
    }
}