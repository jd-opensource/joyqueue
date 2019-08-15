package io.chubao.joyqueue.network.transport.command;

import io.netty.channel.Channel;

/**
 * CommandDispatcher
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/13
 */
public interface CommandDispatcher {

    void dispatch(Channel channel, Command command);
}