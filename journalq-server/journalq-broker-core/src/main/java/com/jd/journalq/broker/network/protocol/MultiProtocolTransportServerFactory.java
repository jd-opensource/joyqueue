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
package com.jd.journalq.broker.network.protocol;

import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.network.protocol.support.DefaultMultiProtocolHandlerPipelineFactory;
import com.jd.journalq.broker.network.protocol.support.DefaultProtocolHandlerPipelineFactory;
import com.jd.journalq.network.transport.command.CommandDispatcherFactory;
import com.jd.journalq.network.transport.command.handler.ExceptionHandler;
import com.jd.journalq.network.transport.command.handler.filter.CommandHandlerFilterFactory;
import com.jd.journalq.network.transport.command.support.DefaultCommandDispatcherFactory;
import com.jd.journalq.network.transport.command.support.DefaultCommandHandlerFilterFactory;
import com.jd.journalq.network.event.TransportEvent;
import com.jd.journalq.network.event.TransportEventHandler;
import com.jd.journalq.network.handler.ConnectionHandler;
import com.jd.journalq.network.transport.RequestBarrier;
import com.jd.journalq.network.transport.TransportServer;
import com.jd.journalq.network.transport.TransportServerFactory;
import com.jd.journalq.network.transport.command.CommandDispatcherFactory;
import com.jd.journalq.network.transport.command.handler.ExceptionHandler;
import com.jd.journalq.network.transport.command.handler.filter.CommandHandlerFilterFactory;
import com.jd.journalq.network.transport.command.support.DefaultCommandDispatcherFactory;
import com.jd.journalq.network.transport.command.support.DefaultCommandHandlerFilterFactory;
import com.jd.journalq.network.transport.config.ServerConfig;
import com.jd.journalq.network.transport.config.TransportConfig;
import com.jd.journalq.toolkit.concurrent.EventBus;

/**
 * 多协议通信工厂
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/14
 */
public class MultiProtocolTransportServerFactory implements TransportServerFactory {

    private TransportConfig transportConfig;
    private EventBus<TransportEvent> transportEventBus;
    private ProtocolManager protocolManager;
    private BrokerContext brokerContext;
    private ExceptionHandler exceptionHandler;
    private RequestBarrier requestBarrier;
    private CommandHandlerFilterFactory commandHandlerFilterFactory;
    private CommandDispatcherFactory commandDispatcherFactory;
    private TransportEventHandler transportEventHandler;
    private ConnectionHandler connectionHandler;
    private MultiProtocolHandlerPipelineFactory multiProtocolHandlerPipelineFactory;
    private ProtocolHandlerPipelineFactory protocolHandlerPipelineFactory;

    public MultiProtocolTransportServerFactory(ProtocolManager protocolManager, BrokerContext brokerContext, EventBus<TransportEvent> transportEventBus, ExceptionHandler exceptionHandler) {
        this.protocolManager = protocolManager;
        this.brokerContext = brokerContext;
        this.transportEventBus = transportEventBus;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public TransportServer bind(ServerConfig serverConfig) {
        return bind(serverConfig, serverConfig.getHost(), serverConfig.getPort());
    }

    @Override
    public TransportServer bind(ServerConfig serverConfig, String host) {
        return bind(serverConfig, host, serverConfig.getPort());
    }

    @Override
    public TransportServer bind(ServerConfig serverConfig, String host, int port) {
        this.transportConfig = serverConfig;
        this.requestBarrier = new RequestBarrier(transportConfig);
        this.commandHandlerFilterFactory = new ProtocolCommandHandlerFilterFactory(brokerContext);
        this.commandDispatcherFactory = new DefaultCommandDispatcherFactory(transportConfig, requestBarrier, commandHandlerFilterFactory, exceptionHandler);
        this.transportEventHandler = new TransportEventHandler(requestBarrier, transportEventBus);
        this.connectionHandler = new ConnectionHandler();
        this.protocolHandlerPipelineFactory = new DefaultProtocolHandlerPipelineFactory(commandDispatcherFactory, transportEventHandler, connectionHandler);
        this.multiProtocolHandlerPipelineFactory = new DefaultMultiProtocolHandlerPipelineFactory(protocolManager, protocolHandlerPipelineFactory);
        return new MultiProtocolTransportServer(serverConfig, host, port, protocolManager, multiProtocolHandlerPipelineFactory, protocolHandlerPipelineFactory);
    }
}