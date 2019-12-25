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
package org.joyqueue.network.transport;

import org.joyqueue.network.transport.config.ClientConfig;
import org.joyqueue.network.transport.exception.TransportException;
import org.joyqueue.toolkit.concurrent.NamedThreadFactory;
import org.joyqueue.toolkit.network.IpUtil;
import org.joyqueue.toolkit.service.Service;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

/**
 * TransportClientSupport
 *
 * author: gaohaoxiang
 * date: 2018/8/13
 */
public abstract class TransportClientSupport extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(TransportClientSupport.class);

    private ClientConfig config;
    private EventLoopGroup ioEventGroup;
    private Bootstrap bootstrap;

    public TransportClientSupport(ClientConfig config) {
        this.config = config;
    }

    @Override
    protected void doStop() {
        if (ioEventGroup != null) {
            ioEventGroup.shutdownGracefully();
        }
    }

    protected Bootstrap newBootstrap(ChannelHandler channelHandler, EventLoopGroup ioEventGroup) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(Epoll.isAvailable() ? EpollSocketChannel.class : NioSocketChannel.class)
                .group(ioEventGroup)
                .handler(channelHandler)
                .option(ChannelOption.SO_REUSEADDR, config.isReuseAddress())
                .option(ChannelOption.SO_RCVBUF, config.getSocketBufferSize())
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        return bootstrap;
    }


    protected EventLoopGroup newIoEventGroup() {
        int ioThread = config.getIoThread();
        NamedThreadFactory threadFactory = new NamedThreadFactory(config.getIoThreadName());
        if (Epoll.isAvailable()) {
            return new EpollEventLoopGroup(ioThread, threadFactory);
        } else {
            return new NioEventLoopGroup(ioThread, threadFactory);
        }
    }

    protected abstract ChannelHandler newChannelHandlerPipeline();

    public ClientConfig getConfig() {
        return config;
    }

    protected synchronized Channel createChannel(SocketAddress address, long connectionTimeout) throws TransportException {
        if (address == null) {
            throw new IllegalArgumentException("address must not be null!");
        }
        if (ioEventGroup == null) {
            ioEventGroup = newIoEventGroup();
        }
        if (bootstrap == null){
            ChannelHandler channelHandlerPipeline = newChannelHandlerPipeline();
            bootstrap = newBootstrap(channelHandlerPipeline, ioEventGroup);
        }
        try {
            long timeout = connectionTimeout > 0 ? connectionTimeout : config.getConnectionTimeout();
            String addr = IpUtil.toAddress(address);
            ChannelFuture channelFuture;
            Channel channel = null;
            channelFuture = bootstrap.connect(address);
            if (!channelFuture.await(timeout)) {
                throw TransportException.ConnectionTimeoutException.build(addr);
            }
            channel = channelFuture.channel();
            if (channel == null || !channel.isActive()) {
                throw TransportException.ConnectionException.build(addr);
            }
            return channel;
        } catch (InterruptedException e) {
            throw TransportException.InterruptedException.build();
        } catch (Exception e) {
            if (e instanceof TransportException) {
                throw (TransportException) e;
            } else {
                throw new TransportException.UnknownException(e.getMessage(), e);
            }
        }
    }


    //TODO IPV6 support
    public static InetSocketAddress createInetSocketAddress(String address) throws TransportException {
        if (address == null || address.isEmpty()) {
            throw new IllegalArgumentException("address must not be empty!");
        }
        String[] parts = address.split("[._:]");
        if (parts.length < 1) {
            throw new IllegalArgumentException("address is invalid.");
        }
        int port;
        try {
            port = Integer.parseInt(parts[parts.length - 1]);
            if (port < 0) {
                throw new IllegalArgumentException("address is invalid.");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("address is invalid.");
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parts.length - 1; i++) {
            if (i > 0) {
                builder.append('.');
            }
            builder.append(parts[i]);
        }
        String ip = builder.toString();
        try {
            return new InetSocketAddress(InetAddress.getByName(ip), port);
        } catch (UnknownHostException e) {
            throw TransportException.UnknownHostException.build(ip);
        }
    }
}