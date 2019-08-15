package io.chubao.joyqueue.broker.network.protocol.support;

import io.chubao.joyqueue.network.event.TransportEventHandler;
import io.chubao.joyqueue.network.handler.ConnectionHandler;
import io.chubao.joyqueue.network.protocol.CommandHandlerProvider;
import io.chubao.joyqueue.network.protocol.Protocol;
import io.chubao.joyqueue.network.transport.codec.Codec;
import io.chubao.joyqueue.network.transport.codec.CodecFactory;
import io.chubao.joyqueue.network.transport.codec.support.NettyDecoder;
import io.chubao.joyqueue.network.transport.codec.support.NettyEncoder;
import io.chubao.joyqueue.network.transport.command.CommandDispatcher;
import io.chubao.joyqueue.network.transport.handler.CommandInvocation;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;

/**
 * DefaultProtocolHandlerPipeline
 *
 * author: gaohaoxiang
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

        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast(new NettyDecoder(codec))
                .addLast(new NettyEncoder(codec))
                .addLast(connectionHandler)
                .addLast(transportEventHandler);

        if (protocol instanceof CommandHandlerProvider) {
            ChannelHandler customHandler = ((CommandHandlerProvider) protocol).getCommandHandler(commandInvocation);
            pipeline.addLast(customHandler);
        } else {
            pipeline.addLast(commandInvocation);
        }
    }

    protected CommandInvocation newCommandInvocation() {
        return new CommandInvocation(commandDispatcher);
    }
}