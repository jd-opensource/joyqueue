package io.chubao.joyqueue.network.protocol;

import io.netty.channel.ChannelHandler;

/**
 * ChannelHandlerProvider
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/16
 */
public interface ChannelHandlerProvider {

    ChannelHandler getChannelHandler(ChannelHandler channelHandler);
}