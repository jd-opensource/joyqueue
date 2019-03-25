package com.jd.journalq.network.event;

import com.jd.journalq.network.transport.RequestBarrier;
import com.jd.journalq.network.transport.TransportHelper;
import com.jd.journalq.toolkit.concurrent.EventBus;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 通信事件处理
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/15
 */
@ChannelHandler.Sharable
public class TransportEventHandler extends ChannelInboundHandlerAdapter {

    private RequestBarrier requestBarrier;
    private EventBus<TransportEvent> eventBus;

    public TransportEventHandler(RequestBarrier requestBarrier, EventBus<TransportEvent> eventBus) {
        this.requestBarrier = requestBarrier;
        this.eventBus = eventBus;
        try {
            this.eventBus.start();
        } catch (Exception e) {
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        eventBus.add(new TransportEvent(TransportEventType.CONNECT, TransportHelper.getOrNewTransport(ctx.channel(), requestBarrier)));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        eventBus.add(new TransportEvent(TransportEventType.CLOSE, TransportHelper.getOrNewTransport(ctx.channel(), requestBarrier)));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        eventBus.add(new TransportEvent(TransportEventType.EXCEPTION, TransportHelper.getOrNewTransport(ctx.channel(), requestBarrier)));
    }
}