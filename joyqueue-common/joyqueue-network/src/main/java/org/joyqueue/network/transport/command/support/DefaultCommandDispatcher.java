/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.network.transport.command.support;

import org.joyqueue.network.transport.RequestBarrier;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.TransportHelper;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.CommandDispatcher;
import org.joyqueue.network.transport.command.Direction;
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