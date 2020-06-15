package com.jd.joyqueue.broker.jmq2.network.protocol;

import com.jd.joyqueue.broker.jmq2.JMQ2Consts;
import com.jd.joyqueue.broker.jmq2.network.helper.JMQ2ProtocolHelper;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.BrokerContextAware;
import org.joyqueue.network.protocol.ExceptionHandlerProvider;
import org.joyqueue.network.protocol.ProtocolService;
import org.joyqueue.network.transport.codec.CodecFactory;
import org.joyqueue.network.transport.command.handler.CommandHandlerFactory;
import org.joyqueue.network.transport.command.handler.ExceptionHandler;
import io.netty.buffer.ByteBuf;

/**
 * jmq协议
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/21
 */
public class JMQ2Protocol implements ProtocolService, BrokerContextAware, ExceptionHandlerProvider {

    private BrokerContext brokerContext;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.brokerContext = brokerContext;
    }

    @Override
    public boolean isSupport(ByteBuf buffer) {
        return JMQ2ProtocolHelper.isSupport(buffer);
    }

    @Override
    public CodecFactory createCodecFactory() {
        return new JMQ2CodecFactory();
    }

    @Override
    public CommandHandlerFactory createCommandHandlerFactory() {
        return new JMQ2CommandHandlerFactory(brokerContext);
    }

    @Override
    public String type() {
        return JMQ2Consts.PROTOCOL_TYPE;
    }

    @Override
    public ExceptionHandler getExceptionHandler() {
        return new JMQ2ExceptionHandler();
    }
}