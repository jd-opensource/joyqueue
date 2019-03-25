package com.jd.journalq.common.network.transport;

import com.jd.journalq.common.network.transport.support.DefaultChannelTransport;
import com.jd.journalq.common.network.transport.support.DefaultTransportAttribute;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

/**
 * 通信管理器
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