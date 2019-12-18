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
package io.chubao.joyqueue.nsr.network;

import io.chubao.joyqueue.network.event.TransportEvent;
import io.chubao.joyqueue.network.transport.codec.Codec;
import io.chubao.joyqueue.network.transport.command.handler.CommandHandlerFactory;
import io.chubao.joyqueue.network.transport.command.handler.ExceptionHandler;
import io.chubao.joyqueue.network.transport.support.DefaultTransportServerFactory;
import io.chubao.joyqueue.nsr.NameService;
import io.chubao.joyqueue.toolkit.concurrent.EventBus;
import io.chubao.joyqueue.toolkit.concurrent.EventListener;
import io.chubao.joyqueue.toolkit.config.PropertySupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author wylixiaobin
 * @date 2019/1/27
 * <p>
 * name server factory
 */
public class NsrTransportServerFactory extends DefaultTransportServerFactory {
    private static NsrCommandHandlerFactory nsrCommandHandlerFactory = new NsrServerCommandHandlerFactory();
    protected static EventBus<TransportEvent> eventBus = new EventBus<>();

    public NsrTransportServerFactory(NameService nameService, PropertySupplier propertySupplier) {
        this(NsrCodecFactory.getInstance(), nsrCommandHandlerFactory, new NsrExceptionHandler(), eventBus);
        nsrCommandHandlerFactory.register(nameService, propertySupplier);
    }

    public NsrTransportServerFactory(Codec codec, CommandHandlerFactory commandHandlerFactory, ExceptionHandler exceptionHandler, EventBus<TransportEvent> eventBus) {
        super(codec, commandHandlerFactory, exceptionHandler, eventBus);
    }

    static class NsrServerCommandHandlerFactory extends NsrCommandHandlerFactory {
        protected static final Logger logger = LoggerFactory.getLogger(NsrCommandHandlerFactory.class);

        @Override
        public String getType() {
            return NsrCommandHandler.SERVER_TYPE;
        }

        @Override
        public void doWithHandler(NsrCommandHandler nsrCommandHandler) {
            if (nsrCommandHandler instanceof EventListener) {
                eventBus.addListener((EventListener<TransportEvent>) nsrCommandHandler);
            }
        }
    }
}
