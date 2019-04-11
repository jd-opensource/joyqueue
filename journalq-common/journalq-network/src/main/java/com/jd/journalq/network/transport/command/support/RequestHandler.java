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
package com.jd.journalq.network.transport.command.support;

import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.handler.CommandHandler;
import com.jd.journalq.network.transport.command.handler.CommandHandlerFactory;
import com.jd.journalq.network.transport.command.handler.ExceptionHandler;
import com.jd.journalq.network.transport.command.handler.filter.CommandHandlerFilterFactory;
import com.jd.journalq.network.transport.command.provider.ExecutorServiceProvider;
import com.jd.journalq.network.transport.Transport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 请求处理器
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/24
 */
public class RequestHandler {

    protected static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private CommandHandlerFactory commandHandlerFactory;
    private CommandHandlerFilterFactory commandHandlerFilterFactory;
    private ExceptionHandler exceptionHandler;

    public RequestHandler(CommandHandlerFactory commandHandlerFactory, CommandHandlerFilterFactory commandHandlerFilterFactory, ExceptionHandler exceptionHandler) {
        this.commandHandlerFactory = commandHandlerFactory;
        this.commandHandlerFilterFactory = commandHandlerFilterFactory;
        this.exceptionHandler = exceptionHandler;
    }

    public void handle(Transport transport, Command command) {
        CommandHandler commandHandler = commandHandlerFactory.getHandler(command);
        if (commandHandler == null) {
            logger.error("unsupported command, command: {}", command);
            return;
        }

        CommandExecuteTask commandExecuteTask = new CommandExecuteTask(transport, command, commandHandler, commandHandlerFilterFactory, exceptionHandler);

        try {
            if (commandHandler instanceof ExecutorServiceProvider) {
                ((ExecutorServiceProvider) commandHandler).getExecutorService(transport, command).execute(commandExecuteTask);
            } else {
                commandExecuteTask.run();
            }
        } catch (Throwable t) {
            logger.error("command handler exception, transport: {}, command: {}", transport, command, t);

            if (exceptionHandler != null) {
                exceptionHandler.handle(transport, command, t);
            }
        }
    }
}