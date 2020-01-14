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
package org.joyqueue.broker.network.protocol.support;

import io.netty.channel.ChannelHandler;
import org.joyqueue.broker.network.protocol.ProtocolHandlerPipelineFactory;
import org.joyqueue.network.event.TransportEventHandler;
import org.joyqueue.network.handler.ConnectionHandler;
import org.joyqueue.network.protocol.ChannelHandlerProvider;
import org.joyqueue.network.protocol.Protocol;
import org.joyqueue.network.transport.command.CommandDispatcher;
import org.joyqueue.network.transport.command.CommandDispatcherFactory;

/**
 * DefaultProtocolHandlerPipelineFactory
 *
 * author: gaohaoxiang
 * date: 2018/8/14
 */
public class DefaultProtocolHandlerPipelineFactory implements ProtocolHandlerPipelineFactory {

    private CommandDispatcherFactory commandDispatcherFactory;
    private TransportEventHandler transportEventHandler;
    private ConnectionHandler connectionHandler;

    public DefaultProtocolHandlerPipelineFactory(CommandDispatcherFactory commandDispatcherFactory, TransportEventHandler transportEventHandler, ConnectionHandler connectionHandler) {
        this.commandDispatcherFactory = commandDispatcherFactory;
        this.transportEventHandler = transportEventHandler;
        this.connectionHandler = connectionHandler;
    }

    @Override
    public ChannelHandler createPipeline(Protocol protocol) {
        CommandDispatcher commandDispatcher = commandDispatcherFactory.getCommandDispatcher(protocol);
        ChannelHandler handlerPipeline = new DefaultProtocolHandlerPipeline(protocol, commandDispatcher, transportEventHandler, connectionHandler);

        if (protocol instanceof ChannelHandlerProvider) {
            ChannelHandler customHandlerPipeline = ((ChannelHandlerProvider) protocol).getChannelHandler(handlerPipeline);
            if (customHandlerPipeline != null) {
                return customHandlerPipeline;
            }
        }

        return handlerPipeline;
    }
}