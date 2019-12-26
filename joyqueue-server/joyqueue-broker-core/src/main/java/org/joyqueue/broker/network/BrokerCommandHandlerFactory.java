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
package org.joyqueue.broker.network;

import com.google.common.collect.Lists;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.BrokerContextAware;
import org.joyqueue.broker.network.support.BrokerCommandHandlerRegistrar;
import org.joyqueue.network.transport.command.support.DefaultCommandHandlerFactory;
import com.jd.laf.extension.ExtensionManager;

import java.util.List;

/**
 * BrokerCommandHandlerFactory
 *
 * author: gaohaoxiang
 * date: 2019/2/28
 */
public class BrokerCommandHandlerFactory extends DefaultCommandHandlerFactory {

    private BrokerContext brokerContext;
    private List<BrokerCommandHandler> commandHandlers;

    public BrokerCommandHandlerFactory(BrokerContext brokerContext) {
        this.brokerContext = brokerContext;
        this.commandHandlers = loadCommandHandlers();
        initCommandHandlers(commandHandlers);
        registerCommandHandlers(commandHandlers);
        BrokerCommandHandlerRegistrar.register(brokerContext, this);
    }

    protected List<BrokerCommandHandler> loadCommandHandlers() {
        return Lists.newArrayList(ExtensionManager.getOrLoadExtensions(BrokerCommandHandler.class));
    }

    protected void initCommandHandlers(List<BrokerCommandHandler> commandHandlers) {
        for (BrokerCommandHandler commandHandler : commandHandlers) {
            if (commandHandler instanceof BrokerContextAware) {
                ((BrokerContextAware) commandHandler).setBrokerContext(brokerContext);
            }
        }
    }

    protected void registerCommandHandlers(List<BrokerCommandHandler> commandHandlers) {
        for (BrokerCommandHandler commandHandler : commandHandlers) {
            super.register(commandHandler);
        }
    }
}