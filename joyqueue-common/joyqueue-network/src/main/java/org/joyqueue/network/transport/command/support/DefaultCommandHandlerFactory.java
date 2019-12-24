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

import com.google.common.collect.Maps;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.network.transport.command.Types;
import org.joyqueue.network.transport.command.handler.CommandHandler;
import org.joyqueue.network.transport.command.handler.CommandHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * DefaultCommandHandlerFactory
 *
 * author: gaohaoxiang
 * date: 2018/8/13
 */
public class DefaultCommandHandlerFactory implements CommandHandlerFactory {

    protected static final Logger logger = LoggerFactory.getLogger(DefaultCommandHandlerFactory.class);

    private Map<Integer /** type **/, CommandHandler> commandHandlerMapper = Maps.newHashMap();

    @Override
    public CommandHandler getHandler(Command command) {
        int commandType = command.getHeader().getType();
        return commandHandlerMapper.get(commandType);
    }

    public void register(CommandHandler commandHandler) {
        if (commandHandler instanceof Types) {
            Types types = (Types) commandHandler;
            for (int type : types.types()) {
                register(type, commandHandler);
            }
        } else if (commandHandler instanceof Type) {
            register(((Type) commandHandler).type(), commandHandler);
        } else {
            logger.warn("unsupported command type, commandHandler: {}", commandHandler);
        }
    }

    public void register(int type, CommandHandler commandHandler) {
        commandHandlerMapper.put(type, commandHandler);
    }
}