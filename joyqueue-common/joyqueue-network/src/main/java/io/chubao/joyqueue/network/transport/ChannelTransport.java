package io.chubao.joyqueue.network.transport;

import io.netty.channel.Channel;

/**
 * ChannelTransport
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/13
 */
public interface ChannelTransport extends Transport {

    Channel getChannel();
}