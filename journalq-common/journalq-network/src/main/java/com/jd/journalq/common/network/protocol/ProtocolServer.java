package com.jd.journalq.common.network.protocol;

import com.jd.journalq.common.network.transport.config.ServerConfig;

/**
 * 协议服务
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/9/25
 */
public interface ProtocolServer extends Protocol {

    ServerConfig createServerConfig(ServerConfig serverConfig);
}