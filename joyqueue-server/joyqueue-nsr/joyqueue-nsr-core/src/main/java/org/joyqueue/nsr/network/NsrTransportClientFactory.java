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
package org.joyqueue.nsr.network;

import org.joyqueue.network.transport.codec.Codec;
import org.joyqueue.network.transport.command.handler.CommandHandlerFactory;
import org.joyqueue.network.transport.command.handler.ExceptionHandler;
import org.joyqueue.network.transport.support.DefaultTransportClientFactory;
import org.joyqueue.nsr.NameService;
import org.joyqueue.toolkit.config.PropertySupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 客户端工厂
 * date: 2018/9/21
 */
public class NsrTransportClientFactory extends DefaultTransportClientFactory {
    private static NsrCommandHandlerFactory nsrCommandHandlerFactory = new NsrClientCommandHandlerFactory();

    public NsrTransportClientFactory() {
        this(nsrCommandHandlerFactory);
    }

    public NsrTransportClientFactory(NameService nameService, PropertySupplier propertySupplier) {
        this(nsrCommandHandlerFactory);
        nsrCommandHandlerFactory.register(nameService, propertySupplier);
    }

    public NsrTransportClientFactory(CommandHandlerFactory commandHandlerFactory) {
        this(commandHandlerFactory, new NsrExceptionHandler());
    }

    public NsrTransportClientFactory(CommandHandlerFactory commandHandlerFactory, ExceptionHandler exceptionHandler) {
        this(NsrCodecFactory.getInstance(), commandHandlerFactory, exceptionHandler);
    }

    public NsrTransportClientFactory(Codec codec) {
        super(codec);
    }

    public NsrTransportClientFactory(Codec codec, CommandHandlerFactory commandHandlerFactory) {
        super(codec, commandHandlerFactory);
    }

    public NsrTransportClientFactory(Codec codec, CommandHandlerFactory commandHandlerFactory, ExceptionHandler exceptionHandler) {
        super(codec, commandHandlerFactory, exceptionHandler);
    }

    static class NsrClientCommandHandlerFactory extends NsrCommandHandlerFactory {

        protected static final Logger logger = LoggerFactory.getLogger(NsrCommandHandlerFactory.class);

        @Override
        public String getType() {
            return NsrCommandHandler.THIN_TYPE;
        }

        @Override
        public void doWithHandler(NsrCommandHandler nsrCommandHandler) {
            //doNothing
        }
    }
}