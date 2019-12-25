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
import org.joyqueue.network.transport.TransportClient;
import org.joyqueue.network.transport.TransportClientFactory;
import org.joyqueue.network.transport.codec.PayloadCodecFactory;
import org.joyqueue.network.transport.codec.support.JoyQueueCodec;
import org.joyqueue.network.transport.config.ClientConfig;
import org.joyqueue.network.transport.support.DefaultTransportClientFactory;
import org.joyqueue.nsr.message.support.network.codec.MessengerHeartbeatRequestCodec;
import org.joyqueue.nsr.message.support.network.codec.MessengerPublishRequestCodec;

/**
 * MessengerTransportClientFactory
 * author: gaohaoxiang
 * date: 2019/8/27
 */
public class MessengerTransportClientFactory implements TransportClientFactory {

    private DefaultTransportClientFactory transportClientFactory;

    public MessengerTransportClientFactory() {
        PayloadCodecFactory payloadCodecFactory = new PayloadCodecFactory();
        payloadCodecFactory.register(new MessengerPublishRequestCodec());
        payloadCodecFactory.register(new MessengerHeartbeatRequestCodec());
        payloadCodecFactory.register(new BooleanAckCodec());

        this.transportClientFactory = new DefaultTransportClientFactory(new JoyQueueCodec(payloadCodecFactory));
    }

    @Override
    public TransportClient create(ClientConfig config) {
        return transportClientFactory.create(config);
    }
}