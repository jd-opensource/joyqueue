package com.jd.journalq.network.handler;

import com.jd.journalq.network.transport.exception.TransportException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 连接处理
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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

        if (cause.getMessage().contains("Connection reset by peer")) {
            logger.error("channel close, address: {}, connection reset by peer", channel.remoteAddress());
            return;
        }

        if (TransportException.isClosed(cause)) {
            logger.error("channel exception, address: {}", channel.remoteAddress(), cause);
        } else {
            logger.error("channel exception, address: {}", channel.remoteAddress());
        }
    }
}