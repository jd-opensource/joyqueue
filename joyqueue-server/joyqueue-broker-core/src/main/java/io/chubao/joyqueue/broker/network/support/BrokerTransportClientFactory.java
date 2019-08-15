package io.chubao.joyqueue.broker.network.support;

import io.chubao.joyqueue.broker.network.codec.BrokerCodecFactory;
import io.chubao.joyqueue.network.transport.codec.Codec;
import io.chubao.joyqueue.network.transport.command.handler.CommandHandlerFactory;
import io.chubao.joyqueue.network.transport.command.handler.ExceptionHandler;
import io.chubao.joyqueue.network.transport.command.support.DefaultCommandHandlerFactory;
import io.chubao.joyqueue.network.transport.support.DefaultTransportClientFactory;

/**
 * BrokerTransportClientFactory
 *
 * author: gaohaoxiang
 * date: 2018/9/21
 */
public class BrokerTransportClientFactory extends DefaultTransportClientFactory {

    public BrokerTransportClientFactory() {
        this(new DefaultCommandHandlerFactory());
    }

    public BrokerTransportClientFactory(CommandHandlerFactory commandHandlerFactory) {
        this(commandHandlerFactory, (ExceptionHandler) null);
    }

    public BrokerTransportClientFactory(CommandHandlerFactory commandHandlerFactory, ExceptionHandler exceptionHandler) {
        this(BrokerCodecFactory.getInstance(), commandHandlerFactory, exceptionHandler);
    }

    public BrokerTransportClientFactory(Codec codec) {
        super(codec);
    }

    public BrokerTransportClientFactory(Codec codec, CommandHandlerFactory commandHandlerFactory) {
        super(codec, commandHandlerFactory);
    }

    public BrokerTransportClientFactory(Codec codec, CommandHandlerFactory commandHandlerFactory, ExceptionHandler exceptionHandler) {
        super(codec, commandHandlerFactory, exceptionHandler);
    }
}