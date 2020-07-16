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
package org.joyqueue.network.transport.command.handler.filter;

import org.apache.commons.collections.CollectionUtils;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.handler.CommandHandler;
import org.joyqueue.network.transport.exception.TransportException;

import java.util.Iterator;
import java.util.List;

/**
 * CommandHandlerInvocation
 *
 * author: gaohaoxiang
 * date: 2018/8/16
 */
public class CommandHandlerInvocation {

    private Transport transport;
    private Command request;
    private CommandHandler commandHandler;
    private Iterator<CommandHandlerFilter> filterIterator;
    private CommandHandlerContext context;

    public CommandHandlerInvocation(Transport transport, Command request, CommandHandler commandHandler, List<CommandHandlerFilter> filterList) {
        this.transport = transport;
        this.request = request;
        this.commandHandler = commandHandler;
        this.filterIterator = (CollectionUtils.isEmpty(filterList) ? null : filterList.iterator());
    }

    public Command invoke() throws TransportException {
        if (filterIterator == null || !filterIterator.hasNext()) {
            return commandHandler.handle(transport, request);
        } else {
            return filterIterator.next().invoke(this);
        }
    }

    public void setTransport(Transport transport) {
        this.transport = transport;
    }

    public Transport getTransport() {
        return transport;
    }

    public Command getRequest() {
        return request;
    }

    public CommandHandlerContext getContext() {
        if (context == null) {
            context = new CommandHandlerContext();
        }
        return context;
    }
}