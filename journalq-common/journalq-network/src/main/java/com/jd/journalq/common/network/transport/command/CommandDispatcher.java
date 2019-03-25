package com.jd.journalq.common.network.transport.command;

import io.netty.channel.Channel;

/**
 * 命令调度器
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/13
 */
public interface CommandDispatcher {

    void dispatch(Channel channel, Command command);
}