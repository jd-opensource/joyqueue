package io.chubao.joyqueue.network.transport.support;

import io.chubao.joyqueue.network.event.TransportEvent;
import io.chubao.joyqueue.network.transport.RequestBarrier;
import io.chubao.joyqueue.network.transport.TransportClient;
import io.chubao.joyqueue.network.transport.TransportClientFactory;
import io.chubao.joyqueue.network.transport.codec.Codec;
import io.chubao.joyqueue.network.transport.command.handler.CommandHandlerFactory;
import io.chubao.joyqueue.network.transport.command.handler.ExceptionHandler;
import io.chubao.joyqueue.network.transport.command.handler.filter.CommandHandlerFilterFactory;
import io.chubao.joyqueue.network.transport.command.support.DefaultCommandHandlerFilterFactory;
import io.chubao.joyqueue.network.transport.command.support.RequestHandler;
import io.chubao.joyqueue.network.transport.command.support.ResponseHandler;
import io.chubao.joyqueue.network.transport.config.ClientConfig;
import io.chubao.joyqueue.toolkit.concurrent.EventBus;

/**
 * DefaultTransportClientFactory
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/24
 */
public class DefaultTransportClientFactory implements TransportClientFactory {

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