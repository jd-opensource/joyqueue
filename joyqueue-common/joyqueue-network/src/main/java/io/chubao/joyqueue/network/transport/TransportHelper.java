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
package io.chubao.joyqueue.network.transport;

import io.chubao.joyqueue.network.transport.support.DefaultChannelTransport;
import io.chubao.joyqueue.network.transport.support.DefaultTransportAttribute;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

/**
 * TransportHelper
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/14
 */
public class TransportHelper {

    private static final AttributeKey<ChannelTransport> TRANSPORT_CACHE_ATTR = AttributeKey.valueOf("TRANSPORT_CACHE");

    public static ChannelTransport getOrNewTransport(Channel channel, RequestBarrier requestBarrier) {
        Attribute<ChannelTransport> attr = channel.attr(TRANSPORT_CACHE_ATTR);
        ChannelTransport transport = attr.get();

        if (transport == null) {
            transport = newTransport(channel, requestBarrier);
            attr.set(transport);
        }

        return transport;
    }

    public static ChannelTransport newTransport(Channel channel, RequestBarrier requestBarrier) {
        TransportAttribute transportAttribute = newTransportAttribute();
        return new DefaultChannelTransport(channel, transportAttribute, requestBarrier);
    }

    public static void setTransport(Channel channel, ChannelTransport transport) {
        channel.attr(TRANSPORT_CACHE_ATTR).set(transport);
    }

    public static ChannelTransport getTransport(Channel channel) {
        return channel.attr(TRANSPORT_CACHE_ATTR).get();
    }

    protected static TransportAttribute newTransportAttribute() {
        return new DefaultTransportAttribute();
    }
}