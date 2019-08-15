package io.chubao.joyqueue.network.handler;

import io.chubao.joyqueue.network.transport.exception.TransportException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ClientConnectionHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/15
 */
@ChannelHandler.Sharable
public class ClientConnectionHandler extends ChannelInboundHandlerAdapter {

    protected static final Logger logger = LoggerFactory.getLogger(ClientConnectionHandler.class);

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
            logger.error("channel is closed, address: {}", channel.remoteAddress());
        } else {
            logger.error("channel exception, address: {}", channel.remoteAddress(), cause);
        }
    }
}