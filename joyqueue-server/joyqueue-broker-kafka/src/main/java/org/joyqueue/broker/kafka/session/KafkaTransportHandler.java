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
package org.joyqueue.broker.kafka.session;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.joyqueue.broker.kafka.command.ProduceRequest;
import org.joyqueue.broker.kafka.config.KafkaConfig;
import org.joyqueue.network.transport.ChannelTransport;
import org.joyqueue.network.transport.RequestBarrier;
import org.joyqueue.network.transport.TransportHelper;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.support.DefaultCommandDispatcher;
import org.joyqueue.network.transport.handler.CommandInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KafkaTransportHandler
 *
 * author: gaohaoxiang
 * date: 2019/5/7
 */
@ChannelHandler.Sharable
public class KafkaTransportHandler extends ChannelDuplexHandler {

    protected static final Logger logger = LoggerFactory.getLogger(KafkaTransportHandler.class);

    private KafkaConfig config;

    public KafkaTransportHandler(KafkaConfig config) {
        this.config = config;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel channel = ctx.channel();
        RequestBarrier requestBarrier = null;
        ChannelTransport transport = TransportHelper.getTransport(channel);

        if (transport == null) {
            if (ctx.pipeline().last() instanceof CommandInvocation) {
                CommandInvocation commandInvocation = (CommandInvocation) ctx.pipeline().last();
                if (commandInvocation.getCommandDispatcher() instanceof DefaultCommandDispatcher) {
                    requestBarrier = ((DefaultCommandDispatcher) commandInvocation.getCommandDispatcher()).getRequestBarrier();
                }
            }

            if (requestBarrier != null) {
                transport = TransportHelper.getOrNewTransport(channel, requestBarrier);
            }
        }

        if (!(transport instanceof KafkaChannelTransport)) {
            KafkaChannelTransport kafkaTransport = new KafkaChannelTransport(transport);
            if (TransportHelper.compareAndSet(channel, transport, kafkaTransport)) {
                transport = kafkaTransport;
            } else {
                transport = TransportHelper.getTransport(channel);
            }
        }

        Command command = (Command) msg;
        if (!(command.getPayload() instanceof ProduceRequest
                && ((ProduceRequest) command.getPayload()).getRequiredAcks() == 0)) {
            ((KafkaChannelTransport) transport).acquire(command);
        }

        super.channelRead(ctx, msg);
    }
}