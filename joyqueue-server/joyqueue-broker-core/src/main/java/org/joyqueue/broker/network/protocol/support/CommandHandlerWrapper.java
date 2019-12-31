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
package org.joyqueue.broker.network.protocol.support;

import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.handler.CommandHandler;
import org.joyqueue.network.transport.command.provider.ExecutorServiceProvider;

import java.util.concurrent.ExecutorService;

/**
 * ProtocolCommandHandler
 * author: gaohaoxiang
 * date: 2019/12/26
 */
public class CommandHandlerWrapper implements CommandHandler, ExecutorServiceProvider {

    private CommandHandler delegate;
    private ExecutorService threadPool;

    public CommandHandlerWrapper(CommandHandler delegate, ExecutorService threadPool) {
        this.delegate = delegate;
        this.threadPool = threadPool;
    }

    @Override
    public Command handle(Transport transport, Command command) {
        return delegate.handle(transport, command);
    }

    @Override
    public ExecutorService getExecutorService(Transport transport, Command command) {
        return threadPool;
    }
}