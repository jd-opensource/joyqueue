package io.chubao.joyqueue.network.transport.handler;

import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.CommandDispatcher;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * CommandInvocation
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
    }

    public CommandDispatcher getCommandDispatcher() {
        return commandDispatcher;
    }
}