package io.chubao.joyqueue.broker.network.support;

import io.chubao.joyqueue.broker.network.codec.BrokerCodecFactory;
import io.chubao.joyqueue.network.event.TransportEvent;
import io.chubao.joyqueue.network.transport.codec.Codec;
import io.chubao.joyqueue.network.transport.command.handler.CommandHandlerFactory;
import io.chubao.joyqueue.network.transport.command.handler.ExceptionHandler;
import io.chubao.joyqueue.network.transport.support.DefaultTransportServerFactory;
import io.chubao.joyqueue.toolkit.concurrent.EventBus;

/**
 * BrokerTransportServerFactory
 *
 * author: gaohaoxiang
 * date: 2018/9/21
 */
public class BrokerTransportServerFactory extends DefaultTransportServerFactory {

    public BrokerTransportServerFactory(CommandHandlerFactory commandHandlerFactory) {
        this(commandHandlerFactory, (ExceptionHandler) null);
    }

    public BrokerTransportServerFactory(CommandHandlerFactory commandHandlerFactory, ExceptionHandler exceptionHandler) {
        this(BrokerCodecFactory.getInstance(), commandHandlerFactory, exceptionHandler);
    }

    public BrokerTransportServerFactory(CommandHandlerFactory commandHandlerFactory, ExceptionHandler exceptionHandler, EventBus<TransportEvent> eventBus) {
        this(BrokerCodecFactory.getInstance(), commandHandlerFactory, exceptionHandler, eventBus);
    }

    public BrokerTransportServerFactory(Codec codec, CommandHandlerFactory commandHandlerFactory) {
        super(codec, commandHandlerFactory);
    }

    public BrokerTransportServerFactory(Codec codec, CommandHandlerFactory commandHandlerFactory, ExceptionHandler exceptionHandler) {
        super(codec, commandHandlerFactory, exceptionHandler);
    }

    public BrokerTransportServerFactory(Codec codec, CommandHandlerFactory commandHandlerFactory, ExceptionHandler exceptionHandler, EventBus<TransportEvent> eventBus) {
        super(codec, commandHandlerFactory, exceptionHandler, eventBus);
    }
}