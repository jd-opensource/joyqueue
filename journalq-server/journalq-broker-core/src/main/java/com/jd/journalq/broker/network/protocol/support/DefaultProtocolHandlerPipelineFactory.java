package com.jd.journalq.broker.network.protocol.support;

import com.jd.journalq.broker.network.protocol.ProtocolHandlerPipelineFactory;
import com.jd.journalq.network.transport.command.CommandDispatcher;
import com.jd.journalq.network.transport.command.CommandDispatcherFactory;
import com.jd.journalq.network.event.TransportEventHandler;
import com.jd.journalq.network.handler.ConnectionHandler;
import com.jd.journalq.network.protocol.ChannelHandlerProvider;
import com.jd.journalq.network.protocol.Protocol;
import io.netty.channel.ChannelHandler;

/**
 * 默认协议处理管道工厂
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/14
 */
public class DefaultProtocolHandlerPipelineFactory implements ProtocolHandlerPipelineFactory {

    private CommandDispatcherFactory commandHandlerFilterFactory;
    private TransportEventHandler transportEventHandler;
    private ConnectionHandler connectionHandler;

    public DefaultProtocolHandlerPipelineFactory(CommandDispatcherFactory commandHandlerFilterFactory, TransportEventHandler transportEventHandler, ConnectionHandler connectionHandler) {
        this.commandHandlerFilterFactory = commandHandlerFilterFactory;
        this.transportEventHandler = transportEventHandler;
        this.connectionHandler = connectionHandler;
    }

    @Override
    public ChannelHandler createPipeline(Protocol protocol) {
        CommandDispatcher commandDispatcher = commandHandlerFilterFactory.getCommandDispatcher(protocol);
        ChannelHandler handlerPipeline = new DefaultProtocolHandlerPipeline(protocol, commandDispatcher, transportEventHandler, connectionHandler);

        if (protocol instanceof ChannelHandlerProvider) {
            handlerPipeline = ((ChannelHandlerProvider) protocol).getChannelHandler(handlerPipeline);
        }

        return handlerPipeline;
    }
}