package com.jd.journalq.network.protocol;

import com.jd.journalq.network.transport.config.ServerConfig;

/**
 * 协议服务
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/9/25
 */
public interface ProtocolServer extends Protocol {

    ServerConfig createServerConfig(ServerConfig serverConfig);
}