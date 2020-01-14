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
package org.joyqueue.broker.network.protocol;

import org.joyqueue.network.protocol.Protocol;
import org.joyqueue.network.protocol.ProtocolService;
import org.joyqueue.network.transport.TransportServer;
import io.netty.channel.ChannelHandler;

public class ProtocolContext {

    private Protocol protocol;
    private ChannelHandler handlerPipeline;
    private TransportServer transportServer;

    public ProtocolContext(ProtocolService protocol, ChannelHandler handlerPipeline) {
        this.protocol = protocol;
        this.handlerPipeline = handlerPipeline;
    }

    public ProtocolContext(Protocol protocol, TransportServer transportServer) {
        this.protocol = protocol;
        this.transportServer = transportServer;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public ChannelHandler getHandlerPipeline() {
        return handlerPipeline;
    }

    public TransportServer getTransportServer() {
        return transportServer;
    }
}