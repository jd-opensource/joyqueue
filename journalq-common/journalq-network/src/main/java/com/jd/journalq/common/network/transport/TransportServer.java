package com.jd.journalq.common.network.transport;

import com.jd.journalq.toolkit.lang.LifeCycle;

import java.net.InetSocketAddress;

/**
 * 通信服务
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/13
 */
public interface TransportServer extends LifeCycle {

    InetSocketAddress getSocketAddress();

    boolean isSSLServer();
}