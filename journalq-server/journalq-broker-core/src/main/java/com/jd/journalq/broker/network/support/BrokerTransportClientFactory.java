package com.jd.journalq.broker.network.support;

import com.jd.journalq.network.transport.codec.Codec;
import com.jd.journalq.network.transport.command.handler.CommandHandlerFactory;
import com.jd.journalq.network.transport.command.handler.ExceptionHandler;
import com.jd.journalq.network.transport.command.support.DefaultCommandHandlerFactory;
import com.jd.journalq.network.transport.support.DefaultTransportClientFactory;
import com.jd.journalq.broker.network.codec.BrokerCodecFactory;

/**
 * 客户端工厂
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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