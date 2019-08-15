package io.chubao.joyqueue.network.protocol;

import io.netty.channel.ChannelHandler;

/**
 * ChannelHandlerProvider
 *
 * author: gaohaoxiang
 * date: 2018/8/16
 */
public interface ChannelHandlerProvider {

    ChannelHandler getChannelHandler(ChannelHandler channelHandler);
}