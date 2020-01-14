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
package org.joyqueue.network.transport.support;

import org.joyqueue.network.transport.TransportServer;
import org.joyqueue.network.transport.TransportServerFactory;
import org.joyqueue.network.transport.config.ServerConfig;
import io.netty.channel.ChannelHandler;

/**
 * ChannelTransportServerFactory
 *
 * author: gaohaoxiang
 * date: 2018/9/25
 */
public class ChannelTransportServerFactory implements TransportServerFactory {

    private ChannelHandler channelHandler;

    public ChannelTransportServerFactory(ChannelHandler channelHandler) {
        this.channelHandler = channelHandler;
    }

    @Override
    public TransportServer bind(ServerConfig serverConfig) {
        return bind(serverConfig, serverConfig.getHost(), serverConfig.getPort());
    }

    @Override
    public TransportServer bind(ServerConfig serverConfig, String host) {
        return bind(serverConfig, host, serverConfig.getPort());
    }

    @Override
    public TransportServer bind(ServerConfig serverConfig, String host, int port) {
        return new ChannelTransportServer(channelHandler, serverConfig, host, port);
    }
}