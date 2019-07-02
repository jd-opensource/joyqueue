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
import com.jd.joyqueue.network.transport.codec.Codec;
import com.jd.joyqueue.network.transport.command.handler.CommandHandlerFactory;
import com.jd.joyqueue.network.transport.command.handler.ExceptionHandler;
import com.jd.joyqueue.network.transport.command.support.DefaultCommandHandlerFactory;
import com.jd.joyqueue.network.transport.support.DefaultTransportClientFactory;

/**
 * BrokerTransportClientFactory
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/9/21
 */
public class BrokerTransportClientFactory extends DefaultTransportClientFactory {

    public BrokerTransportClientFactory() {
        this(new DefaultCommandHandlerFactory());
    }

    public BrokerTransportClientFactory(CommandHandlerFactory commandHandlerFactory) {
        this(commandHandlerFactory, (ExceptionHandler) null);
    }

    public BrokerTransportClientFactory(CommandHandlerFactory commandHandlerFactory, ExceptionHandler exceptionHandler) {
        this(BrokerCodecFactory.getInstance(), commandHandlerFactory, exceptionHandler);
    }

    public BrokerTransportClientFactory(Codec codec) {
        super(codec);
    }

    public BrokerTransportClientFactory(Codec codec, CommandHandlerFactory commandHandlerFactory) {
        super(codec, commandHandlerFactory);
    }

    public BrokerTransportClientFactory(Codec codec, CommandHandlerFactory commandHandlerFactory, ExceptionHandler exceptionHandler) {
        super(codec, commandHandlerFactory, exceptionHandler);
    }
}