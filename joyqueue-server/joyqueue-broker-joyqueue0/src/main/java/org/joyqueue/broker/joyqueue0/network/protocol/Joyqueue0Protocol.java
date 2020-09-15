package org.joyqueue.broker.joyqueue0.network.protocol;

import org.joyqueue.broker.joyqueue0.Joyqueue0Consts;
import org.joyqueue.broker.joyqueue0.network.helper.Joyqueue0ProtocolHelper;
import io.netty.buffer.ByteBuf;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.BrokerContextAware;
import org.joyqueue.network.protocol.ExceptionHandlerProvider;
import org.joyqueue.network.protocol.ProtocolService;
import org.joyqueue.network.transport.codec.CodecFactory;
import org.joyqueue.network.transport.command.handler.CommandHandlerFactory;
import org.joyqueue.network.transport.command.handler.ExceptionHandler;

/**
 * joyqueue0协议
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/21
 */
public class Joyqueue0Protocol implements ProtocolService, BrokerContextAware, ExceptionHandlerProvider {

    private BrokerContext brokerContext;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.brokerContext = brokerContext;
    }

    @Override
    public boolean isSupport(ByteBuf buffer) {
        return Joyqueue0ProtocolHelper.isSupport(buffer);
    }

    @Override
    public CodecFactory createCodecFactory() {
        return new Joyqueue0CodecFactory();
    }

    @Override
    public CommandHandlerFactory createCommandHandlerFactory() {
        return new Joyqueue0CommandHandlerFactory(brokerContext);
    }

    @Override
    public String type() {
        return Joyqueue0Consts.PROTOCOL_TYPE;
    }

    @Override
    public ExceptionHandler getExceptionHandler() {
        return new Joyqueue0ExceptionHandler();
    }
}