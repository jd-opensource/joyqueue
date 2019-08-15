package io.chubao.joyqueue.network.transport.command.support;

import io.chubao.joyqueue.network.transport.RequestBarrier;
import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.TransportHelper;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.CommandDispatcher;
import io.chubao.joyqueue.network.transport.command.Direction;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DefaultCommandDispatcher
 *
 * author: gaohaoxiang
 * date: 2018/8/13
 */
public class DefaultCommandDispatcher implements CommandDispatcher {

    protected static final Logger logger = LoggerFactory.getLogger(DefaultCommandDispatcher.class);

    private RequestBarrier requestBarrier;
    private RequestHandler requestHandler;
    private ResponseHandler responseHandler;

    public DefaultCommandDispatcher(RequestBarrier requestBarrier, RequestHandler requestHandler, ResponseHandler responseHandler) {
        this.requestBarrier = requestBarrier;
        this.requestHandler = requestHandler;
        this.responseHandler = responseHandler;
    }

    @Override
    public void dispatch(Channel channel, Command command) {
        Transport transport = TransportHelper.getOrNewTransport(channel, requestBarrier);
        Direction direction = command.getHeader().getDirection();

        //logger.info("request context, channel: {}, command: {}, header: {}", channel, command, command.getHeader());

        if (direction.equals(Direction.REQUEST)) {
            requestHandler.handle(transport, command);
        } else if (direction.equals(Direction.RESPONSE)) {
            responseHandler.handle(transport, command);
        } else {
            logger.error("unsupported direction, direction: {}, transport: {}, command: {}", direction, transport, command);
        }
    }

    public RequestBarrier getRequestBarrier() {
        return requestBarrier;
    }
}