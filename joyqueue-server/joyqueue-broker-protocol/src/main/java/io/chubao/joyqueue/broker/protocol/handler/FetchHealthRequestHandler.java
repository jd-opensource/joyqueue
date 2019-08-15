package io.chubao.joyqueue.broker.protocol.handler;

import io.chubao.joyqueue.broker.protocol.JoyQueueCommandHandler;
import io.chubao.joyqueue.network.command.FetchHealthResponse;
import io.chubao.joyqueue.network.command.JoyQueueCommandType;
import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.Type;

/**
 * FetchHealthRequestHandler
 *
 * author: gaohaoxiang
 * date: 2018/12/28
 */
public class FetchHealthRequestHandler implements JoyQueueCommandHandler, Type {

    @Override
    public Command handle(Transport transport, Command command) {
        FetchHealthResponse fetchHealthResponse = new FetchHealthResponse(0);
        return new Command(fetchHealthResponse);
    }

    @Override
    public int type() {
        return JoyQueueCommandType.FETCH_HEALTH_REQUEST.getCode();
    }
}