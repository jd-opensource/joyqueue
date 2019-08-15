package io.chubao.joyqueue.network.protocol;

import io.chubao.joyqueue.network.transport.config.ServerConfig;

/**
 * ProtocolServer
 *
 * author: gaohaoxiang
 * date: 2018/9/25
 */
public interface ProtocolServer extends Protocol {

    ServerConfig createServerConfig(ServerConfig serverConfig);
}