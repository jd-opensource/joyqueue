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
package org.joyqueue.nsr.message.support.network.transport;

import org.joyqueue.network.codec.BooleanAckCodec;
import org.joyqueue.network.transport.TransportServer;
import org.joyqueue.network.transport.TransportServerFactory;
import org.joyqueue.network.transport.codec.PayloadCodecFactory;
import org.joyqueue.network.transport.codec.support.JoyQueueCodec;
import org.joyqueue.network.transport.command.support.DefaultCommandHandlerFactory;
import org.joyqueue.network.transport.config.ServerConfig;
import org.joyqueue.network.transport.support.DefaultTransportServerFactory;
import org.joyqueue.nsr.config.MessengerConfig;
import org.joyqueue.nsr.message.support.network.codec.MessengerHeartbeatRequestCodec;
import org.joyqueue.nsr.message.support.network.codec.MessengerPublishRequestCodec;
import org.joyqueue.nsr.message.support.network.handler.MessengerHeartbeatRequestHandler;
import org.joyqueue.nsr.message.support.network.handler.MessengerPublishRequestHandler;
import org.joyqueue.toolkit.concurrent.EventBus;

/**
 * MessengerTransportServerFactory
 * author: gaohaoxiang
 * date: 2019/8/27
 */
public class MessengerTransportServerFactory implements TransportServerFactory {

    private TransportServerFactory transportServerFactory;
    private MessengerConfig config;
    private EventBus eventBus;

    public MessengerTransportServerFactory(MessengerConfig config, EventBus eventBus) {
        PayloadCodecFactory payloadCodecFactory = new PayloadCodecFactory();
        payloadCodecFactory.register(new MessengerPublishRequestCodec());
        payloadCodecFactory.register(new MessengerHeartbeatRequestCodec());
        payloadCodecFactory.register(new BooleanAckCodec());

        DefaultCommandHandlerFactory commandHandlerFactory = new DefaultCommandHandlerFactory();
        commandHandlerFactory.register(new MessengerPublishRequestHandler(config, eventBus));
        commandHandlerFactory.register(new MessengerHeartbeatRequestHandler());

        this.eventBus = eventBus;
        this.transportServerFactory = new DefaultTransportServerFactory(new JoyQueueCodec(payloadCodecFactory), commandHandlerFactory);
    }

    @Override
    public TransportServer bind(ServerConfig serverConfig) {
        return transportServerFactory.bind(serverConfig);
    }

    @Override
    public TransportServer bind(ServerConfig serverConfig, String host) {
        return transportServerFactory.bind(serverConfig, host);
    }

    @Override
    public TransportServer bind(ServerConfig serverConfig, String host, int port) {
        return transportServerFactory.bind(serverConfig, host, port);
    }
}