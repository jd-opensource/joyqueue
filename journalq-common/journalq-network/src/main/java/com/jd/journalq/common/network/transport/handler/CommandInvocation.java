package com.jd.journalq.common.network.transport.handler;

import com.jd.journalq.common.network.transport.command.Command;
import com.jd.journalq.common.network.transport.command.CommandDispatcher;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 命令调用器
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/13
 */
@ChannelHandler.Sharable
public class CommandInvocation extends SimpleChannelInboundHandler<Command> {

    private CommandDispatcher commandDispatcher;

    public CommandInvocation(CommandDispatcher commandDispatcher) {
        this.commandDispatcher = commandDispatcher;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command command) throws Exception {
        commandDispatcher.dispatch(ctx.channel(), command);
        if (ctx.pipeline().last() != this) {
            ctx.fireChannelRead(command);
        }
    }
}