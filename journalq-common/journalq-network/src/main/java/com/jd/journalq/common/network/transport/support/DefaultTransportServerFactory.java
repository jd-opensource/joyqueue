package com.jd.journalq.common.network.transport.support;

import com.jd.journalq.common.network.transport.codec.Codec;
import com.jd.journalq.common.network.transport.command.handler.CommandHandlerFactory;
import com.jd.journalq.common.network.transport.command.handler.ExceptionHandler;
import com.jd.journalq.common.network.transport.command.handler.filter.CommandHandlerFilterFactory;
import com.jd.journalq.common.network.transport.command.support.DefaultCommandHandlerFilterFactory;
import com.jd.journalq.common.network.transport.command.support.RequestHandler;
import com.jd.journalq.common.network.transport.command.support.ResponseHandler;
import com.jd.journalq.common.network.event.TransportEvent;
import com.jd.journalq.common.network.transport.RequestBarrier;
import com.jd.journalq.common.network.transport.TransportServer;
import com.jd.journalq.common.network.transport.TransportServerFactory;
import com.jd.journalq.common.network.transport.config.ServerConfig;
import com.jd.journalq.toolkit.concurrent.EventBus;

/**
 * 默认通信服务工厂
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/22
 */
public class DefaultTransportServerFactory implements TransportServerFactory {

    private Codec codec;
    private CommandHandlerFactory commandHandlerFactory;
    private ExceptionHandler exceptionHandler;
    private EventBus<TransportEvent> eventBus;

    public DefaultTransportServerFactory(Codec codec, CommandHandlerFactory commandHandlerFactory) {
        this(codec, commandHandlerFactory, null);
    }

    public DefaultTransportServerFactory(Codec codec, CommandHandlerFactory commandHandlerFactory, ExceptionHandler exceptionHandler) {
        this(codec, commandHandlerFactory, exceptionHandler, new EventBus());
    }

    public DefaultTransportServerFactory(Codec codec, CommandHandlerFactory commandHandlerFactory, ExceptionHandler exceptionHandler, EventBus<TransportEvent> eventBus) {
        this.codec = codec;
        this.commandHandlerFactory = commandHandlerFactory;
        this.exceptionHandler = exceptionHandler;
        this.eventBus = eventBus;
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
        CommandHandlerFilterFactory commandHandlerFilterFactory = new DefaultCommandHandlerFilterFactory();
        RequestBarrier requestBarrier = new RequestBarrier(serverConfig);
        RequestHandler requestHandler = new RequestHandler(commandHandlerFactory, commandHandlerFilterFactory, exceptionHandler);
        ResponseHandler responseHandler = new ResponseHandler(serverConfig, requestBarrier, exceptionHandler);
        return new DefaultTransportServer(serverConfig, host, port, codec, requestBarrier, requestHandler, responseHandler, eventBus);
    }
}