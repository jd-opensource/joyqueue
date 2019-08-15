package io.chubao.joyqueue.network.transport.support;

import io.chubao.joyqueue.network.transport.TransportServer;
import io.chubao.joyqueue.network.transport.TransportServerFactory;
import io.chubao.joyqueue.network.transport.config.ServerConfig;
import io.netty.channel.ChannelHandler;

/**
 * ChannelTransportServerFactory
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/9/25
 */
public class ChannelTransportServerFactory implements TransportServerFactory {

    private ChannelHandler channelHandler;

    public ChannelTransportServerFactory(ChannelHandler channelHandler) {
        this.channelHandler = channelHandler;
    }

    @Override
    public TransportServer bind(ServerConfig serverConfig) {
        return bind(serverConfig, serverConfig.getHost(), serverConfig.getPort());
    }

    @Override
    public TransportServer bind(ServerConfig serverConfig, String host) {
        return bind(serverConfig, host, serverConfig.getPort());
    }

    @Override
    public TransportServer bind(ServerConfig serverConfig, String host, int port) {
        return new ChannelTransportServer(channelHandler, serverConfig, host, port);
    }
}