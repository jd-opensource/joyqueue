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
package org.joyqueue.network.transport;

import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.joyqueue.network.transport.support.DefaultChannelTransport;

/**
 * TransportHelper
 *
 * author: gaohaoxiang
 * date: 2018/8/14
 */
public class TransportHelper {

    private static final AttributeKey<ChannelTransport> TRANSPORT_CACHE_ATTR = AttributeKey.valueOf("TRANSPORT_CACHE");

    public static ChannelTransport getOrNewTransport(Channel channel, RequestBarrier requestBarrier) {
        Attribute<ChannelTransport> attr = channel.attr(TRANSPORT_CACHE_ATTR);
        ChannelTransport transport = attr.get();

        if (transport == null) {
            transport = newTransport(channel, requestBarrier);
            if (!attr.compareAndSet(null, transport)) {
                transport = attr.get();
            }
        }

        return transport;
    }

    public static ChannelTransport newTransport(Channel channel, RequestBarrier requestBarrier) {
        return new DefaultChannelTransport(channel, requestBarrier);
    }

    public static void setTransport(Channel channel, ChannelTransport transport) {
        channel.attr(TRANSPORT_CACHE_ATTR).set(transport);
    }

    public static boolean compareAndSet(Channel channel, ChannelTransport oldTransport, ChannelTransport newTransport) {
        return channel.attr(TRANSPORT_CACHE_ATTR).compareAndSet(oldTransport, newTransport);
    }

    public static ChannelTransport getTransport(Channel channel) {
        return channel.attr(TRANSPORT_CACHE_ATTR).get();
    }
}