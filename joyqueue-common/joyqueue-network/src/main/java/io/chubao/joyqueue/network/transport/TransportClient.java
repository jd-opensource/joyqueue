package io.chubao.joyqueue.network.transport;

import io.chubao.joyqueue.network.event.TransportEvent;
import io.chubao.joyqueue.network.transport.exception.TransportException;
import io.chubao.joyqueue.toolkit.concurrent.EventListener;
import io.chubao.joyqueue.toolkit.lang.LifeCycle;

import java.net.SocketAddress;

/**
 * TransportClient
 *
 * author: gaohaoxiang
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
    Transport createTransport(String address) throws TransportException;

    /**
     * 创建连接，阻塞直到成功或失败
     *
     * @param address           地址
     * @param connectionTimeout 连接超时
     * @return 通道
     * @throws TransportException
     */
    Transport createTransport(String address, long connectionTimeout) throws TransportException;

    /**
     * 创建连接，阻塞直到成功或失败
     *
     * @param address 地址
     * @return 通道
     * @throws TransportException
     */
    Transport createTransport(SocketAddress address) throws TransportException;

    /**
     * 创建连接
     *
     * @param address           地址
     * @param connectionTimeout 连接超时
     * @return 通道
     * @throws TransportException
     */
    Transport createTransport(SocketAddress address, long connectionTimeout) throws TransportException;

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