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

import org.joyqueue.network.event.TransportEvent;
import org.joyqueue.network.transport.RequestBarrier;
import org.joyqueue.network.transport.TransportClient;
import org.joyqueue.network.transport.TransportClientFactory;
import org.joyqueue.network.transport.codec.Codec;
import org.joyqueue.network.transport.codec.support.JoyQueueCodec;
import org.joyqueue.network.transport.command.handler.CommandHandlerFactory;
import org.joyqueue.network.transport.command.handler.ExceptionHandler;
import org.joyqueue.network.transport.command.handler.filter.CommandHandlerFilterFactory;
import org.joyqueue.network.transport.command.support.DefaultCommandHandlerFilterFactory;
import org.joyqueue.network.transport.command.support.RequestHandler;
import org.joyqueue.network.transport.command.support.ResponseHandler;
import org.joyqueue.network.transport.config.ClientConfig;
import org.joyqueue.toolkit.concurrent.EventBus;

/**
 * DefaultTransportClientFactory
 *
 * author: gaohaoxiang
 * date: 2018/8/24
 */
public class DefaultTransportClientFactory implements TransportClientFactory {

    private Codec codec;
    private CommandHandlerFactory commandHandlerFactory;
    private ExceptionHandler exceptionHandler;
    private EventBus<TransportEvent> transportEventBus;

    public DefaultTransportClientFactory() {
        this(new JoyQueueCodec());
    }

    public DefaultTransportClientFactory(Codec codec) {
        this(codec, (CommandHandlerFactory) null);
    }

    public DefaultTransportClientFactory(Codec codec, EventBus<TransportEvent> transportEventBus) {
        this(codec, null, null, transportEventBus);
    }

    public DefaultTransportClientFactory(Codec codec, CommandHandlerFactory commandHandlerFactory) {
        this(codec, commandHandlerFactory, null);
    }

    public DefaultTransportClientFactory(Codec codec, CommandHandlerFactory commandHandlerFactory, ExceptionHandler exceptionHandler) {
        this(codec, commandHandlerFactory, exceptionHandler, new EventBus());
    }

    public DefaultTransportClientFactory(Codec codec, CommandHandlerFactory commandHandlerFactory, ExceptionHandler exceptionHandler, EventBus<TransportEvent> transportEventBus) {
        this.codec = codec;
        this.commandHandlerFactory = commandHandlerFactory;
        this.exceptionHandler = exceptionHandler;
        this.transportEventBus = transportEventBus;
    }

    @Override
    public TransportClient create(ClientConfig config) {
        CommandHandlerFilterFactory commandHandlerFilterFactory = new DefaultCommandHandlerFilterFactory();
        RequestBarrier requestBarrier = new RequestBarrier(config);
        RequestHandler requestHandler = new RequestHandler(commandHandlerFactory, commandHandlerFilterFactory, exceptionHandler);
        ResponseHandler responseHandler = new ResponseHandler(config, requestBarrier, exceptionHandler);
        DefaultTransportClient transportClient = new DefaultTransportClient(config, codec, requestBarrier, requestHandler, responseHandler, transportEventBus);
        return new FailoverTransportClient(transportClient, config, transportEventBus);
    }
}