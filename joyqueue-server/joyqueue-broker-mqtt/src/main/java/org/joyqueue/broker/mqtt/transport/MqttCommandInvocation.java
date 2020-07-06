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
package org.joyqueue.broker.mqtt.transport;

import org.joyqueue.broker.mqtt.cluster.MqttConnectionManager;
import org.joyqueue.broker.mqtt.cluster.MqttConsumerManager;
import org.joyqueue.broker.mqtt.cluster.MqttSessionManager;
import org.joyqueue.broker.mqtt.connection.MqttConnection;
import org.joyqueue.broker.mqtt.handler.ExecutorsProvider;
import org.joyqueue.broker.mqtt.handler.Handler;
import org.joyqueue.broker.mqtt.handler.HandlerExecutor;
import org.joyqueue.broker.mqtt.handler.MqttHandlerDispatcher;
import org.joyqueue.broker.mqtt.util.NettyAttrManager;
import com.google.common.base.Strings;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.channel.ChannelFutureListener.CLOSE;
import static io.netty.channel.ChannelFutureListener.CLOSE_ON_FAILURE;

/**
 * @author majun8
 */
@ChannelHandler.Sharable
public class MqttCommandInvocation extends SimpleChannelInboundHandler<Object> {
    private final Logger LOG = LoggerFactory.getLogger(MqttCommandInvocation.class);

    private MqttHandlerDispatcher mqttHandlerDispatcher;
    private MqttConnectionManager connectionManager;
    private MqttSessionManager sessionManager;
    private MqttConsumerManager consumerManager;

    public MqttCommandInvocation(MqttHandlerDispatcher mqttHandlerDispatcher) {
        this.mqttHandlerDispatcher = mqttHandlerDispatcher;
        this.connectionManager = mqttHandlerDispatcher.getMqttProtocolHandler().getConnectionManager();
        this.sessionManager = mqttHandlerDispatcher.getMqttProtocolHandler().getSessionManager();
        this.consumerManager = mqttHandlerDispatcher.getMqttProtocolHandler().getConsumerManager();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (ctx.channel().isActive()) {
                if (msg instanceof MqttMessage) {
                    MqttMessage message = (MqttMessage) msg;
                    if (message.decoderResult().isSuccess()) {
                        Handler handler = mqttHandlerDispatcher.getHandler(message.fixedHeader().messageType());
                        HandlerExecutor executor = new HandlerExecutor(handler, ctx, message);
                        if (handler instanceof ExecutorsProvider) {
                            ((ExecutorsProvider) handler).getExecutorService().submit(executor);
                        } else {
                            executor.execute();
                        }
                    }
                }
            } else {
                LOG.error("The channel is not active!" + ctx.channel());
            }
        } catch (Throwable th) {
            LOG.error("MqttCommandInvocation got exception: ", th);
            ctx.fireExceptionCaught(th);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {}

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        String clientId = NettyAttrManager.getAttrClientId(ctx.channel());
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state().equals(IdleState.READER_IDLE)) {
                // keepalive的1.5倍时间内没有收到client端写操作 触发inactive并关闭连接
                LOG.info("READER_IDLE: {}, start close channel...", clientId);
                ctx.fireChannelInactive();
                ctx.close().addListener(CLOSE_ON_FAILURE);
            } else if (event.state().equals(IdleState.WRITER_IDLE)) {
                //未进行写操作
                LOG.info("WRITER_IDLE: {}, start close channel...", clientId);
            } else if (event.state().equals(IdleState.ALL_IDLE)) {
                //未进行读写
                LOG.info("ALL_IDLE: {}, start close channel...", clientId);
                ctx.fireChannelInactive();
                ctx.close().addListener(CLOSE_ON_FAILURE);
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String clientId = NettyAttrManager.getAttrClientId(ctx.channel());
        if (!Strings.isNullOrEmpty(clientId)) {
            MqttConnection connection = connectionManager.getConnection(clientId);
            if (connection == null) {
                ctx.channel().close().addListener(CLOSE_ON_FAILURE);
                return;
            }
            if (!(ctx.channel().equals(connection.getChannel()))) {
                ctx.channel().close().addListener(CLOSE_ON_FAILURE);
                return;
            }
            // 掉线情况下清理client的订阅消费 连接也清理
            consumerManager.stopConsume(clientId);
            sessionManager.removeSession(clientId);
            connectionManager.removeConnection(connection);
        }

        ctx.close().addListener(CLOSE);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        String clientId = NettyAttrManager.getAttrClientId(ctx.channel());
        LOG.info("Channel Writable Changed, clientID: {}", clientId);
        ctx.fireChannelWritabilityChanged();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        String clientId = NettyAttrManager.getAttrClientId(ctx.channel());
        if (!Strings.isNullOrEmpty(clientId)) {
            LOG.info("Exception got clientID: {}, cause: {}, {}", clientId, cause.getCause(), cause.getMessage());
            consumerManager.stopConsume(clientId);
            sessionManager.removeSession(clientId);

            MqttConnection connection = connectionManager.getConnection(clientId);
            if (connection != null) {
                connection.getChannel().close().addListener(CLOSE_ON_FAILURE);
                connectionManager.removeConnection(connection);
                return;
            }

            ctx.close().addListener(CLOSE_ON_FAILURE);
        }
    }
}
