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
package com.jd.journalq.broker.protocol.network;

import com.google.common.collect.Lists;
import com.jd.journalq.broker.BrokerContextAware;
import com.jd.journalq.broker.protocol.JournalqCommandHandler;
import com.jd.journalq.broker.protocol.JournalqContext;
import com.jd.journalq.broker.protocol.JournalqContextAware;
import com.jd.journalq.network.transport.command.support.DefaultCommandHandlerFactory;
import com.jd.laf.extension.ExtensionManager;

import java.util.List;

/**
 * JournalqCommandHandlerFactory
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/28
 */
public class JournalqCommandHandlerFactory extends DefaultCommandHandlerFactory {

    private JournalqContext journalqContext;
    private List<JournalqCommandHandler> commandHandlers;

    public JournalqCommandHandlerFactory(JournalqContext journalqContext) {
        this.journalqContext = journalqContext;
        this.commandHandlers = loadCommandHandlers();
        initCommandHandlers(commandHandlers);
        registerCommandHandlers(commandHandlers);
    }

    protected List<JournalqCommandHandler> loadCommandHandlers() {
        return Lists.newArrayList(ExtensionManager.getOrLoadExtensions(JournalqCommandHandler.class));
    }

    protected void initCommandHandlers(List<JournalqCommandHandler> commandHandlers) {
        for (JournalqCommandHandler commandHandler : commandHandlers) {
            if (commandHandler instanceof BrokerContextAware) {
                ((BrokerContextAware) commandHandler).setBrokerContext(journalqContext.getBrokerContext());
            }
            if (commandHandler instanceof JournalqContextAware) {
                ((JournalqContextAware) commandHandler).setJournalqContext(journalqContext);
            }
        }
    }

    protected void registerCommandHandlers(List<JournalqCommandHandler> commandHandlers) {
        for (JournalqCommandHandler commandHandler : commandHandlers) {
            super.register(commandHandler);
        }
    }
}