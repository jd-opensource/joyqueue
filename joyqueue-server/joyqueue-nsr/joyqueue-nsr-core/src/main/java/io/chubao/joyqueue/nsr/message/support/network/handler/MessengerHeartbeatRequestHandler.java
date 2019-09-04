package io.chubao.joyqueue.nsr.message.support.network.handler;

import io.chubao.joyqueue.network.command.BooleanAck;
import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.network.transport.command.handler.CommandHandler;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;

/**
 * MessengerHeartbeatRequestHandler
 * author: gaohaoxiang
 * date: 2019/8/27
 */
public class MessengerHeartbeatRequestHandler implements CommandHandler, Type {

    @Override
    public Command handle(Transport transport, Command command) {
        return BooleanAck.build();
    }

    @Override
    public int type() {
        return NsrCommandType.NSR_MESSENGER_HEARTBEAT_REQUEST;
    }
}