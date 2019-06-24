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
package com.jd.journalq.broker.network;

import com.google.common.collect.Lists;
import com.jd.journalq.broker.BrokerContext;
import com.jd.journalq.broker.BrokerContextAware;
import com.jd.journalq.broker.network.support.BrokerCommandHandlerRegistrar;
import com.jd.journalq.network.transport.command.support.DefaultCommandHandlerFactory;
import com.jd.laf.extension.ExtensionManager;

import java.util.List;

/**
 * BrokerCommandHandlerFactory
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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