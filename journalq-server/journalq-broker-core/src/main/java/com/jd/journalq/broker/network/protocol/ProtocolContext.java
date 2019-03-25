package com.jd.journalq.broker.network.protocol;

import com.jd.journalq.common.network.protocol.Protocol;
import com.jd.journalq.common.network.protocol.ProtocolService;
import com.jd.journalq.common.network.transport.TransportServer;
import io.netty.channel.ChannelHandler;

public class ProtocolContext {

    private Protocol protocol;
    private ChannelHandler handlerPipeline;
    private TransportServer transportServer;

    public ProtocolContext(ProtocolService protocol, ChannelHandler handlerPipeline) {
        this.protocol = protocol;
        this.handlerPipeline = handlerPipeline;
    }

    public ProtocolContext(Protocol protocol, TransportServer transportServer) {
        this.protocol = protocol;
        this.transportServer = transportServer;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public ChannelHandler getHandlerPipeline() {
        return handlerPipeline;
    }

    public TransportServer getTransportServer() {
        return transportServer;
    }
}