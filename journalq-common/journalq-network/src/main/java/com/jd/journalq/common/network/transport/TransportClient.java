package com.jd.journalq.common.network.transport;

import com.jd.journalq.common.network.event.TransportEvent;
import com.jd.journalq.common.network.transport.exception.TransportException;
import com.jd.journalq.toolkit.concurrent.EventListener;
import com.jd.journalq.toolkit.lang.LifeCycle;

import java.net.SocketAddress;

/**
 * 通信服务
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/13
 */
public interface TransportClient extends LifeCycle {

    /**
     * 创建连接，阻塞直到成功或失败
     *
     * @param address 地址
     * @return 通道
     * @throws TransportException
     */
    Transport createTransport(final String address) throws TransportException;

    /**
     * 创建连接，阻塞直到成功或失败
     *
     * @param address           地址
     * @param connectionTimeout 连接超时
     * @return 通道
     * @throws TransportException
     */
    Transport createTransport(final String address, final long connectionTimeout) throws TransportException;

    /**
     * 创建连接，阻塞直到成功或失败
     *
     * @param address 地址
     * @return 通道
     * @throws TransportException
     */
    Transport createTransport(final SocketAddress address) throws TransportException;

    /**
     * 创建连接
     *
     * @param address           地址
     * @param connectionTimeout 连接超时
     * @return 通道
     * @throws TransportException
     */
    Transport createTransport(final SocketAddress address, final long connectionTimeout) throws TransportException;

    /**
     * 添加监听器
     *
     * @param listener
     */
    void addListener(EventListener<TransportEvent> listener);

    /**
     * 移除监听器
     *
     * @param listener
     */
    void removeListener(EventListener<TransportEvent> listener);
}