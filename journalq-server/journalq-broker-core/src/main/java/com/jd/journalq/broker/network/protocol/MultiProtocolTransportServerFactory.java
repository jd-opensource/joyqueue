package com.jd.journalq.broker.network.protocol;

import com.jd.journalq.broker.network.protocol.support.DefaultMultiProtocolHandlerPipelineFactory;
import com.jd.journalq.broker.network.protocol.support.DefaultProtocolHandlerPipelineFactory;
import com.jd.journalq.common.network.transport.command.CommandDispatcherFactory;
import com.jd.journalq.common.network.transport.command.handler.ExceptionHandler;
import com.jd.journalq.common.network.transport.command.handler.filter.CommandHandlerFilterFactory;
import com.jd.journalq.common.network.transport.command.support.DefaultCommandDispatcherFactory;
import com.jd.journalq.common.network.transport.command.support.DefaultCommandHandlerFilterFactory;
import com.jd.journalq.common.network.event.TransportEvent;
import com.jd.journalq.common.network.event.TransportEventHandler;
import com.jd.journalq.common.network.handler.ConnectionHandler;
import com.jd.journalq.common.network.transport.RequestBarrier;
import com.jd.journalq.common.network.transport.TransportServer;
import com.jd.journalq.common.network.transport.TransportServerFactory;
import com.jd.journalq.common.network.transport.config.ServerConfig;
import com.jd.journalq.common.network.transport.config.TransportConfig;
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
    private ExceptionHandler exceptionHandler;
    private RequestBarrier requestBarrier;
    private CommandHandlerFilterFactory commandHandlerFilterFactory;
    private CommandDispatcherFactory commandDispatcherFactory;
    private TransportEventHandler transportEventHandler;
    private ConnectionHandler connectionHandler;
    private MultiProtocolHandlerPipelineFactory multiProtocolHandlerPipelineFactory;
    private ProtocolHandlerPipelineFactory protocolHandlerPipelineFactory;

    public MultiProtocolTransportServerFactory(ProtocolManager protocolManager, EventBus<TransportEvent> transportEventBus, ExceptionHandler exceptionHandler) {
        this.protocolManager = protocolManager;
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
        this.commandHandlerFilterFactory = new DefaultCommandHandlerFilterFactory();
        this.commandDispatcherFactory = new DefaultCommandDispatcherFactory(transportConfig, requestBarrier, commandHandlerFilterFactory, exceptionHandler);
        this.transportEventHandler = new TransportEventHandler(requestBarrier, transportEventBus);
        this.connectionHandler = new ConnectionHandler();
        this.protocolHandlerPipelineFactory = new DefaultProtocolHandlerPipelineFactory(commandDispatcherFactory, transportEventHandler, connectionHandler);
        this.multiProtocolHandlerPipelineFactory = new DefaultMultiProtocolHandlerPipelineFactory(protocolManager, protocolHandlerPipelineFactory);
        return new MultiProtocolTransportServer(serverConfig, host, port, protocolManager, multiProtocolHandlerPipelineFactory, protocolHandlerPipelineFactory);
    }
}