/**
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
package io.chubao.joyqueue.broker.network.protocol.support;

import io.chubao.joyqueue.broker.network.protocol.ProtocolHandlerPipelineFactory;
import io.chubao.joyqueue.network.event.TransportEventHandler;
import io.chubao.joyqueue.network.handler.ConnectionHandler;
import io.chubao.joyqueue.network.protocol.ChannelHandlerProvider;
import io.chubao.joyqueue.network.protocol.Protocol;
import io.chubao.joyqueue.network.transport.command.CommandDispatcher;
import io.chubao.joyqueue.network.transport.command.CommandDispatcherFactory;
import io.netty.channel.ChannelHandler;

/**
 * DefaultProtocolHandlerPipelineFactory
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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
            handlerPipeline = ((ChannelHandlerProvider) protocol).getChannelHandler(handlerPipeline);
        }

        return handlerPipeline;
    }
}