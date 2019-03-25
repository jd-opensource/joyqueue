package com.jd.journalq.broker.mqtt.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author majun8
 */
public class HandlerExecutor implements Runnable {
    private final static Logger LOG = LoggerFactory.getLogger(HandlerExecutor.class);

    private Handler handler;
    private ChannelHandlerContext context;
    private MqttMessage message;

    public HandlerExecutor(final Handler handler, final ChannelHandlerContext context, final MqttMessage message) {
        this.handler = handler;
        this.context = context;
        this.message = message;
    }

    @Override
    public void run() {
        execute();
    }

    public void execute() {
        try {
            if (handler != null) {
                handler.handleRequest(context.channel(), message);
            }
        } catch (Throwable th) {
            LOG.error("HandlerExecutor got exception: ", th);
            context.fireExceptionCaught(th);
        } finally {
            ReferenceCountUtil.release(message);
        }
    }
}
