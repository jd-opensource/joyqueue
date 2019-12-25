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
package org.joyqueue.broker.network.support;

import org.joyqueue.broker.network.codec.BrokerCodecFactory;
import org.joyqueue.network.transport.TransportClient;
import org.joyqueue.network.transport.TransportClientFactory;
import org.joyqueue.network.transport.command.support.DefaultCommandHandlerFactory;
import org.joyqueue.network.transport.config.ClientConfig;
import org.joyqueue.network.transport.support.DefaultTransportClientFactory;

/**
 * BrokerTransportClientFactory
 *
 * author: gaohaoxiang
 * date: 2018/9/21
 */
public class BrokerTransportClientFactory implements TransportClientFactory {

    private DefaultTransportClientFactory transportClientFactory;

    public BrokerTransportClientFactory() {
        transportClientFactory = new DefaultTransportClientFactory(BrokerCodecFactory.getInstance(),
                new DefaultCommandHandlerFactory());
    }

    @Override
    public TransportClient create(ClientConfig config) {
        return transportClientFactory.create(config);
    }
}