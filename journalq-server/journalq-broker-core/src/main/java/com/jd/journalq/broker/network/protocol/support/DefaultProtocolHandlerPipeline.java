package com.jd.journalq.broker.network.protocol.support;

import com.jd.journalq.common.network.transport.codec.Codec;
import com.jd.journalq.common.network.transport.codec.CodecFactory;
import com.jd.journalq.common.network.transport.codec.support.NettyDecoder;
import com.jd.journalq.common.network.transport.codec.support.NettyEncoder;
import com.jd.journalq.common.network.transport.command.CommandDispatcher;
import com.jd.journalq.common.network.event.TransportEventHandler;
import com.jd.journalq.common.network.handler.ConnectionHandler;
import com.jd.journalq.common.network.protocol.Protocol;
import com.jd.journalq.common.network.transport.handler.CommandInvocation;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;

/**
 * 协议处理器管道
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/13
 */
@ChannelHandler.Sharable
public class DefaultProtocolHandlerPipeline extends ChannelInitializer {

    private Protocol protocol;
    private CommandDispatcher commandDispatcher;
    private TransportEventHandler transportEventHandler;
    private ConnectionHandler connectionHandler;
    private CommandInvocation commandInvocation;
    private Codec codec;

    public DefaultProtocolHandlerPipeline(Protocol protocol, CommandDispatcher commandDispatcher, TransportEventHandler transportEventHandler, ConnectionHandler connectionHandler) {
        this.protocol = protocol;
        this.commandDispatcher = commandDispatcher;
        this.transportEventHandler = transportEventHandler;
        this.connectionHandler = connectionHandler;
        this.commandInvocation = newCommandInvocation();

        CodecFactory codecFactory = protocol.createCodecFactory();
        if (codecFactory != null) {
            this.codec = codecFactory.getCodec();
        }
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        if (codec == null) {
            throw new IllegalArgumentException(String.format("codec is null, protocol: %s", protocol.type()));
        }

        channel.pipeline()
                .addLast(new NettyDecoder(codec))
                .addLast(new NettyEncoder(codec))
                .addLast(connectionHandler)
                .addLast(transportEventHandler)
                .addLast(commandInvocation);
    }

    protected CommandInvocation newCommandInvocation() {
        return new CommandInvocation(commandDispatcher);
    }
}