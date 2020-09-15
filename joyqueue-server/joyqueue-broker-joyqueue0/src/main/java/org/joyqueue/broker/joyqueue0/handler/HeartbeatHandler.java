package org.joyqueue.broker.joyqueue0.handler;

import org.joyqueue.broker.joyqueue0.Joyqueue0CommandHandler;
import org.joyqueue.broker.joyqueue0.Joyqueue0CommandType;
import org.joyqueue.broker.joyqueue0.command.BooleanAck;
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
public class HeartbeatHandler implements Joyqueue0CommandHandler, Type {

    @Override
    public Command handle(Transport transport, Command command) throws TransportException {
        return BooleanAck.build();
    }

    @Override
    public int type() {
        return Joyqueue0CommandType.HEARTBEAT.getCode();
    }
}