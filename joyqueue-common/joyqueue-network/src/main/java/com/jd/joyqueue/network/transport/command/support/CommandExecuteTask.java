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
package com.jd.joyqueue.network.transport.command.support;

import com.jd.joyqueue.network.transport.command.Command;
import com.jd.joyqueue.network.transport.command.handler.CommandHandler;
import com.jd.joyqueue.network.transport.command.handler.ExceptionHandler;
import com.jd.joyqueue.network.transport.command.handler.filter.CommandHandlerFilter;
import com.jd.joyqueue.network.transport.command.handler.filter.CommandHandlerFilterFactory;
import com.jd.joyqueue.network.transport.command.handler.filter.CommandHandlerInvocation;
import com.jd.joyqueue.network.transport.Transport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 命令执行线程
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/14
 */
public class CommandExecuteTask implements Runnable {

    protected static final Logger logger = LoggerFactory.getLogger(CommandExecuteTask.class);

    private Transport transport;
    private Command request;
    private CommandHandler commandHandler;
    private CommandHandlerFilterFactory commandHandlerFilterFactory;
    private ExceptionHandler exceptionHandler;

    public CommandExecuteTask(Transport transport, Command request, CommandHandler commandHandler, CommandHandlerFilterFactory commandHandlerFilterFactory, ExceptionHandler exceptionHandler) {
        this.transport = transport;
        this.request = request;
        this.commandHandler = commandHandler;
        this.commandHandlerFilterFactory = commandHandlerFilterFactory;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public void run() {
        try {
            List<CommandHandlerFilter> commandHandlerFilters = commandHandlerFilterFactory.getFilters();
            CommandHandlerInvocation commandHandlerInvocation = new CommandHandlerInvocation(transport, request, commandHandler, commandHandlerFilters);
            Command response = commandHandlerInvocation.invoke();

            if (response != null) {
                transport.acknowledge(request, response);
            }
        } catch (Throwable t) {
            logger.error("command handler exception, tratnsport: {}, command: {}", transport, request, t);

            if (exceptionHandler != null) {
                exceptionHandler.handle(transport, request, t);
            }
        }
    }
}