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

import org.joyqueue.network.transport.config.ServerConfig;
import org.joyqueue.toolkit.concurrent.NamedThreadFactory;
import org.joyqueue.toolkit.service.Service;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * TransportServerSupport
 *
 * author: gaohaoxiang
 * date: 2018/8/13
 */
public abstract class TransportServerSupport extends Service implements TransportServer {

    protected static final Logger logger = LoggerFactory.getLogger(TransportServerSupport.class);

    private ServerConfig config;
    private String host;
    private int port;
    private EventLoopGroup acceptEventGroup;
    private EventLoopGroup ioEventGroup;
    private ServerBootstrap serverBootstrap;
    private Channel channel;

    public TransportServerSupport(ServerConfig config) {
        this.config = config;
        this.host = config.getHost();
        this.port = config.getPort();
    }

    public TransportServerSupport(ServerConfig config, String host) {
        this.config = config;
        this.host = host;
        this.port = config.getPort();
    }

    public TransportServerSupport(ServerConfig config, String host, int port) {
        this.config = config;
        this.host = host;
        this.port = port;
    }

    @Override
    protected void doStart() throws Exception {
        EventLoopGroup acceptEventGroup = newAcceptEventGroup();
        EventLoopGroup ioEventGroup = newIoEventGroup();
        ChannelHandler channelHandlerPipeline = newChannelHandlerPipeline();
        ServerBootstrap serverBootstrap = newBootstrap(channelHandlerPipeline, acceptEventGroup, ioEventGroup);
        Channel channel = doBind(serverBootstrap);

        this.acceptEventGroup = acceptEventGroup;
        this.ioEventGroup = ioEventGroup;
        this.serverBootstrap = serverBootstrap;
        this.channel = channel;
    }

    @Override
    protected void doStop() {
        if (acceptEventGroup != null) {
            acceptEventGroup.shutdownGracefully();
        }
        if (ioEventGroup != null) {
            ioEventGroup.shutdownGracefully();
        }
        if (channel != null) {
            channel.close();
        }
    }

    @Override
    public InetSocketAddress getSocketAddress() {
        return new InetSocketAddress(host, port);
    }

    @Override
    public boolean isSSLServer() {
        return false;
    }

    protected ServerBootstrap newBootstrap(ChannelHandler channelHandler, EventLoopGroup acceptEventGroup, EventLoopGroup ioEventGroup) throws Exception {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.channel(Epoll.isAvailable() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .group(acceptEventGroup, ioEventGroup)
                .childHandler(channelHandler)
                .option(ChannelOption.SO_REUSEADDR, config.isReuseAddress())
                .option(ChannelOption.SO_RCVBUF, config.getSocketBufferSize())
                .option(ChannelOption.SO_BACKLOG, config.getBacklog())
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.SO_SNDBUF, config.getSocketBufferSize())
                .childOption(ChannelOption.TCP_NODELAY, config.isTcpNoDelay())
                .childOption(ChannelOption.SO_KEEPALIVE, config.isKeepAlive())
                .childOption(ChannelOption.SO_LINGER, config.getSoLinger())
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        return serverBootstrap;
    }

    protected Channel doBind(ServerBootstrap serverBootstrap) throws Exception {
        return serverBootstrap.bind(port)
                .sync()
                .channel();
    }

    protected EventLoopGroup newAcceptEventGroup() {
        NamedThreadFactory threadFactory = new NamedThreadFactory(config.getAcceptThreadName());
        if (Epoll.isAvailable()) {
            return new EpollEventLoopGroup(config.getAcceptThread(), threadFactory);
        } else {
            return new NioEventLoopGroup(config.getAcceptThread(), threadFactory);
        }
    }

    protected EventLoopGroup newIoEventGroup() {
        NamedThreadFactory threadFactory = new NamedThreadFactory(config.getIoThreadName());
        if (Epoll.isAvailable()) {
            return new EpollEventLoopGroup(config.getIoThread(), threadFactory);
        } else {
            return new NioEventLoopGroup(config.getIoThread(), threadFactory);
        }
    }

    protected abstract ChannelHandler newChannelHandlerPipeline();

    public ServerConfig getConfig() {
        return config;
    }

    public Channel getChannel() {
        return channel;
    }
}