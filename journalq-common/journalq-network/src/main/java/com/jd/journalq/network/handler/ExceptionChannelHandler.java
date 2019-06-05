package com.jd.journalq.network.handler;

import com.jd.journalq.network.transport.RequestBarrier;
import com.jd.journalq.network.transport.TransportHelper;
import com.jd.journalq.network.transport.command.handler.ExceptionHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * ExceptionChannelHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/5
 */
public class ExceptionChannelHandler extends ChannelHandlerAdapter {

    private ExceptionHandler exceptionHandler;
    private RequestBarrier requestBarrier;

    public ExceptionChannelHandler(ExceptionHandler exceptionHandler, RequestBarrier requestBarrier) {
        this.exceptionHandler = exceptionHandler;
        this.requestBarrier = requestBarrier;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (exceptionHandler != null) {
            exceptionHandler.handle(TransportHelper.getOrNewTransport(ctx.channel(), requestBarrier), null, cause);
        } else {
            super.exceptionCaught(ctx, cause);
        }
    }
}