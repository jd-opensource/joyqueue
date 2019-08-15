package io.chubao.joyqueue.network.transport;

import io.chubao.joyqueue.toolkit.lang.LifeCycle;

import java.net.InetSocketAddress;

/**
 * TransportServer
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/13
 */
public interface TransportServer extends LifeCycle {

    InetSocketAddress getSocketAddress();

    boolean isSSLServer();
}