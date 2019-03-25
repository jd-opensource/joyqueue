package com.jd.journalq.network.transport;

import com.jd.journalq.network.transport.config.ClientConfig;

/**
 * 客户端通信工厂
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/24
 */
public interface TransportClientFactory {

    TransportClient create(ClientConfig config);
}