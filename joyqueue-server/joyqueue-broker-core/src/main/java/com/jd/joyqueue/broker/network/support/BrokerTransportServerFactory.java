/**
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
package com.jd.joyqueue.broker.network.support;

import com.jd.joyqueue.broker.network.codec.BrokerCodecFactory;
import com.jd.joyqueue.network.event.TransportEvent;
import com.jd.joyqueue.network.transport.codec.Codec;
import com.jd.joyqueue.network.transport.command.handler.CommandHandlerFactory;
import com.jd.joyqueue.network.transport.command.handler.ExceptionHandler;
import com.jd.joyqueue.network.transport.support.DefaultTransportServerFactory;
import com.jd.joyqueue.toolkit.concurrent.EventBus;

/**
 * 服务端工厂
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/9/21
 */
public class BrokerTransportServerFactory extends DefaultTransportServerFactory {

    public BrokerTransportServerFactory(CommandHandlerFactory commandHandlerFactory) {
        this(commandHandlerFactory, (ExceptionHandler) null);
    }

    public BrokerTransportServerFactory(CommandHandlerFactory commandHandlerFactory, ExceptionHandler exceptionHandler) {
        this(BrokerCodecFactory.getInstance(), commandHandlerFactory, exceptionHandler);
    }

    public BrokerTransportServerFactory(CommandHandlerFactory commandHandlerFactory, ExceptionHandler exceptionHandler, EventBus<TransportEvent> eventBus) {
        this(BrokerCodecFactory.getInstance(), commandHandlerFactory, exceptionHandler, eventBus);
    }

    public BrokerTransportServerFactory(Codec codec, CommandHandlerFactory commandHandlerFactory) {
        super(codec, commandHandlerFactory);
    }

    public BrokerTransportServerFactory(Codec codec, CommandHandlerFactory commandHandlerFactory, ExceptionHandler exceptionHandler) {
        super(codec, commandHandlerFactory, exceptionHandler);
    }

    public BrokerTransportServerFactory(Codec codec, CommandHandlerFactory commandHandlerFactory, ExceptionHandler exceptionHandler, EventBus<TransportEvent> eventBus) {
        super(codec, commandHandlerFactory, exceptionHandler, eventBus);
    }
}