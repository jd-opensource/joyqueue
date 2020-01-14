/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.broker.network.protocol.support;

import org.joyqueue.network.event.TransportEventHandler;
import org.joyqueue.network.handler.ConnectionHandler;
import org.joyqueue.network.protocol.CommandHandlerProvider;
import org.joyqueue.network.protocol.Protocol;
import org.joyqueue.network.transport.codec.Codec;
import org.joyqueue.network.transport.codec.CodecFactory;
import org.joyqueue.network.transport.codec.support.NettyDecoder;
import org.joyqueue.network.transport.codec.support.NettyEncoder;
import org.joyqueue.network.transport.command.CommandDispatcher;
import org.joyqueue.network.transport.handler.CommandInvocation;
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
            if (customHandler != null) {
                pipeline.addLast(customHandler);
            } else {
                pipeline.addLast(commandInvocation);
            }
        } else {
            pipeline.addLast(commandInvocation);
        }
    }

    protected CommandInvocation newCommandInvocation() {
        return new CommandInvocation(commandDispatcher);
    }
}