package com.jd.journalq.common.network.transport.support;

import com.jd.journalq.common.network.event.TransportEvent;
import com.jd.journalq.common.network.event.TransportEventHandler;
import com.jd.journalq.common.network.handler.ClientConnectionHandler;
import com.jd.journalq.common.network.transport.RequestBarrier;
import com.jd.journalq.common.network.transport.Transport;
import com.jd.journalq.common.network.transport.TransportClient;
import com.jd.journalq.common.network.transport.TransportClientSupport;
import com.jd.journalq.common.network.transport.TransportHelper;
import com.jd.journalq.common.network.transport.codec.Codec;
import com.jd.journalq.common.network.transport.codec.support.NettyDecoder;
import com.jd.journalq.common.network.transport.codec.support.NettyEncoder;
import com.jd.journalq.common.network.transport.command.CommandDispatcher;
import com.jd.journalq.common.network.transport.command.support.DefaultCommandDispatcher;
import com.jd.journalq.common.network.transport.command.support.RequestHandler;
import com.jd.journalq.common.network.transport.command.support.ResponseHandler;
import com.jd.journalq.common.network.transport.config.ClientConfig;
import com.jd.journalq.common.network.transport.exception.TransportException;
import com.jd.journalq.common.network.transport.handler.CommandInvocation;
import com.jd.journalq.toolkit.concurrent.EventBus;
import com.jd.journalq.toolkit.concurrent.EventListener;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;

import java.net.SocketAddress;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 默认通信客户端
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/24
 */
public class DefaultTransportClient extends TransportClientSupport implements TransportClient {

    private Codec codec;
    private RequestBarrier requestBarrier;
    private RequestHandler requestHandler;
    private ResponseHandler responseHandler;
    private EventBus<TransportEvent> transportEventBus;
    private Timer clearTimer;

    public DefaultTransportClient(ClientConfig config, Codec codec, final RequestBarrier requestBarrier, RequestHandler requestHandler, ResponseHandler responseHandler, EventBus<TransportEvent> transportEventBus) {
        super(config);
        this.codec = codec;
        this.requestBarrier = requestBarrier;
        this.requestHandler = requestHandler;
        this.responseHandler = responseHandler;
        this.transportEventBus = transportEventBus;
        this.clearTimer = new Timer("jmq-client-clear-timer");

        // TODO 延迟和调度时间
        this.clearTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                requestBarrier.evict();
            }
        }, 1000 * 3, 1000);
    }

    @Override
    protected ChannelHandler newChannelHandlerPipeline() {
        final CommandDispatcher commandDispatcher = new DefaultCommandDispatcher(requestBarrier, requestHandler, responseHandler);
        return new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel) {
                channel.pipeline()
                        .addLast(new NettyDecoder(codec))
                        .addLast(new NettyEncoder(codec))
                        .addLast(new ClientConnectionHandler())
                        .addLast(new TransportEventHandler(requestBarrier, transportEventBus))
                        .addLast(new CommandInvocation(commandDispatcher));
            }
        };
    }

    @Override
    public Transport createTransport(String address) throws TransportException {
        return this.createTransport(address, -1);
    }

    @Override
    public Transport createTransport(String address, long connectionTimeout) throws TransportException {
        return this.createTransport(createInetSocketAddress(address), connectionTimeout);
    }

    @Override
    public Transport createTransport(SocketAddress address) throws TransportException {
        return this.createTransport(address, -1);
    }

    @Override
    public Transport createTransport(SocketAddress address, long connectionTimeout) throws TransportException {
        Channel channel = createChannel(address, connectionTimeout);
        return TransportHelper.newTransport(channel, requestBarrier);
    }

    @Override
    public void addListener(EventListener<TransportEvent> listener) {
        this.transportEventBus.addListener(listener);
    }

    @Override
    public void removeListener(EventListener<TransportEvent> listener) {
        this.transportEventBus.removeListener(listener);
    }

    @Override
    protected void doStop() {
        super.doStop();
        requestBarrier.clear();
    }
}