package com.jd.journalq.network.protocol;

import io.netty.channel.ChannelHandler;

/**
 * 通道处理提供
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/16
 */
public interface ChannelHandlerProvider {

    ChannelHandler getChannelHandler(ChannelHandler channelHandler);
}