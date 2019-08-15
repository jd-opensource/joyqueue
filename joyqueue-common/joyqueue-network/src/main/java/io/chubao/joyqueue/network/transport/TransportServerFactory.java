package io.chubao.joyqueue.network.transport;

import io.chubao.joyqueue.network.transport.config.ServerConfig;

/**
 * TransportServerFactory
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/13
 */
public interface TransportServerFactory {

    TransportServer bind(ServerConfig serverConfig);

    TransportServer bind(ServerConfig serverConfig, String host);

    TransportServer bind(ServerConfig serverConfig, String host, int port);
}