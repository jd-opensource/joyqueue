package com.jd.journalq.common.network.transport.support;

import com.jd.journalq.common.network.event.TransportEvent;
import com.jd.journalq.common.network.transport.RequestBarrier;
import com.jd.journalq.common.network.transport.TransportClient;
import com.jd.journalq.common.network.transport.TransportClientFactory;
import com.jd.journalq.common.network.transport.codec.Codec;
import com.jd.journalq.common.network.transport.command.handler.CommandHandlerFactory;
import com.jd.journalq.common.network.transport.command.handler.ExceptionHandler;
import com.jd.journalq.common.network.transport.command.handler.filter.CommandHandlerFilterFactory;
import com.jd.journalq.common.network.transport.command.support.DefaultCommandHandlerFilterFactory;
import com.jd.journalq.common.network.transport.command.support.RequestHandler;
import com.jd.journalq.common.network.transport.command.support.ResponseHandler;
import com.jd.journalq.common.network.transport.config.ClientConfig;
import com.jd.journalq.toolkit.concurrent.EventBus;

/**
 * 默认通信客户端工厂
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/24
 */
public class  DefaultTransportClientFactory implements TransportClientFactory {

    private Codec codec;
    private CommandHandlerFactory commandHandlerFactory;
    private ExceptionHandler exceptionHandler;
    private EventBus<TransportEvent> transportEventBus;

    public DefaultTransportClientFactory(Codec codec) {
        this(codec, (CommandHandlerFactory) null);
    }

    public DefaultTransportClientFactory(Codec codec, EventBus<TransportEvent> transportEventBus) {
        this(codec, null, null, transportEventBus);
    }

    public DefaultTransportClientFactory(Codec codec, CommandHandlerFactory commandHandlerFactory) {
        this(codec, commandHandlerFactory, null);
    }

    public DefaultTransportClientFactory(Codec codec, CommandHandlerFactory commandHandlerFactory, ExceptionHandler exceptionHandler) {
        this(codec, commandHandlerFactory, exceptionHandler, new EventBus());
    }

    public DefaultTransportClientFactory(Codec codec, CommandHandlerFactory commandHandlerFactory, ExceptionHandler exceptionHandler, EventBus<TransportEvent> transportEventBus) {
        this.codec = codec;
        this.commandHandlerFactory = commandHandlerFactory;
        this.exceptionHandler = exceptionHandler;
        this.transportEventBus = transportEventBus;
    }

    @Override
    public TransportClient create(ClientConfig config) {
        CommandHandlerFilterFactory commandHandlerFilterFactory = new DefaultCommandHandlerFilterFactory();
        RequestBarrier requestBarrier = new RequestBarrier(config);
        RequestHandler requestHandler = new RequestHandler(commandHandlerFactory, commandHandlerFilterFactory, exceptionHandler);
        ResponseHandler responseHandler = new ResponseHandler(config, requestBarrier, exceptionHandler);
        DefaultTransportClient transportClient = new DefaultTransportClient(config, codec, requestBarrier, requestHandler, responseHandler, transportEventBus);
        return new FailoverTransportClient(transportClient, config, transportEventBus);
    }
}