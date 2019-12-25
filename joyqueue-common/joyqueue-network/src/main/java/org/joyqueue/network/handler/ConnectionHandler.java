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
package org.joyqueue.network.handler;

import org.joyqueue.network.transport.exception.TransportException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ConnectionHandler
 *
 * author: gaohaoxiang
 * date: 2018/8/15
 */
@ChannelHandler.Sharable
public class ConnectionHandler extends ChannelInboundHandlerAdapter {

    protected static final Logger logger = LoggerFactory.getLogger(ConnectionHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        logger.info("connection is connected, address: {}", ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        logger.info("connection is closed, address: {}", ctx.channel().remoteAddress());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Channel channel = ctx.channel();
        try {
            channel.close().await();
        } catch (InterruptedException ignored) {

        }

        if (TransportException.isClosed(cause)) {
            logger.warn("channel close, address: {}, message: {}", channel.remoteAddress(), cause.getMessage());
        } else {
            logger.error("channel exception, address: {}", channel.remoteAddress(), cause);
        }
    }
}