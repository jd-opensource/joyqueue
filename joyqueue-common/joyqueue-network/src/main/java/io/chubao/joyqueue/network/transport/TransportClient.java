/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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