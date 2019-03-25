package com.jd.journalq.common.network.transport.command.support;

import com.jd.journalq.common.network.transport.command.Command;
import com.jd.journalq.common.network.transport.command.CommandDispatcher;
import com.jd.journalq.common.network.transport.command.Direction;
import com.jd.journalq.common.network.transport.RequestBarrier;
import com.jd.journalq.common.network.transport.Transport;
import com.jd.journalq.common.network.transport.TransportHelper;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 命令调度器
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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
}