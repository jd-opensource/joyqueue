package io.chubao.joyqueue.network.transport;

import io.chubao.joyqueue.network.transport.config.ClientConfig;

/**
 * TransportClientFactory
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/24
 */
public interface TransportClientFactory {

    TransportClient create(ClientConfig config);
}