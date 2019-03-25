package com.jd.journalq.common.network.transport;

import io.netty.channel.Channel;

/**
 * 通达通道
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/13
 */
public interface ChannelTransport extends Transport {

    Channel getChannel();
}