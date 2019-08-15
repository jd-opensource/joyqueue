package io.chubao.joyqueue.network.transport.command;

import io.netty.channel.Channel;

/**
 * CommandDispatcher
 *
 * author: gaohaoxiang
 * date: 2018/8/13
 */
public interface CommandDispatcher {

    void dispatch(Channel channel, Command command);
}