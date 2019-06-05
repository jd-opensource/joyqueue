/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jd.journalq.network.transport.handler;

import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.CommandDispatcher;
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
    }

    public CommandDispatcher getCommandDispatcher() {
        return commandDispatcher;
    }

    public CommandDispatcher getCommandDispatcher() {
        return commandDispatcher;
    }
}