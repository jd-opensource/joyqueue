/**
 * Copyright 2019 The JoyQueue Authors.
 *
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
package org.joyqueue.network.transport.command.support;

import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.handler.CommandHandler;
import org.joyqueue.network.transport.command.handler.CommandHandlerFactory;
import org.joyqueue.network.transport.command.handler.ExceptionHandler;
import org.joyqueue.network.transport.command.handler.filter.CommandHandlerFilterFactory;
import org.joyqueue.network.transport.command.provider.ExecutorServiceProvider;
import org.joyqueue.network.transport.Transport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RequestHandler
 *
 * author: gaohaoxiang
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

    public void handle(Transport transport, Command request) {
        CommandHandler commandHandler = commandHandlerFactory.getHandler(request);
        if (commandHandler == null) {
            logger.error("unsupported command, request: {}", request);
            return;
        }

        CommandExecuteTask commandExecuteTask = new CommandExecuteTask(transport, request, commandHandler, commandHandlerFilterFactory, exceptionHandler);

        try {
            if (commandHandler instanceof ExecutorServiceProvider) {
                ((ExecutorServiceProvider) commandHandler).getExecutorService(transport, request).execute(commandExecuteTask);
            } else {
                commandExecuteTask.run();
            }
        } catch (Throwable t) {
            if (exceptionHandler == null) {
                logger.error("command handler exception, transport: {}, request: {}", transport, request, t);
            } else {
                exceptionHandler.handle(transport, request, t);
            }
        }
    }
}