package io.chubao.joyqueue.broker.kafka.session;

import io.chubao.joyqueue.broker.kafka.KafkaCommandType;
import io.chubao.joyqueue.broker.kafka.command.ProduceRequest;
import io.chubao.joyqueue.broker.kafka.config.KafkaConfig;
import io.chubao.joyqueue.network.transport.ChannelTransport;
import io.chubao.joyqueue.network.transport.RequestBarrier;
import io.chubao.joyqueue.network.transport.TransportHelper;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.support.DefaultCommandDispatcher;
import io.chubao.joyqueue.network.transport.handler.CommandInvocation;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

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
            transport = new KafkaChannelTransport(transport);
            TransportHelper.setTransport(channel, transport);
        }

        int type = ((Command) msg).getHeader().getType();
        if (type == KafkaCommandType.METADATA.getCode()
                || type == KafkaCommandType.FIND_COORDINATOR.getCode()
                || type == KafkaCommandType.LIST_OFFSETS.getCode()
                || type == KafkaCommandType.PRODUCE.getCode()
                || type == KafkaCommandType.FETCH.getCode()) {
            if (!((KafkaChannelTransport) transport).tryAcquire(config.getTransportAcquireTimeout(), TimeUnit.MILLISECONDS)) {
                logger.warn("transport acquire failed, transport: {}, type: {}", transport, type);
            }
        } else {
            ((KafkaChannelTransport) transport).tryAcquire();
        }

        if (((Command) msg).getPayload() instanceof ProduceRequest
                && ((ProduceRequest) ((Command) msg).getPayload()).getRequiredAcks() == 0) {
            ((KafkaChannelTransport) transport).release();
        }

        super.channelRead(ctx, msg);
    }
}