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
package com.jd.joyqueue.broker.jmq2.network.protocol;

import com.google.common.collect.Lists;
import com.jd.joyqueue.broker.jmq2.JMQ2CommandHandler;
import com.jd.laf.extension.ExtensionManager;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.BrokerContextAware;
import org.joyqueue.network.transport.command.support.DefaultCommandHandlerFactory;

import java.util.List;

/**
 * JMQ2CommandHandlerFactory
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/2/28
 */
public class JMQ2CommandHandlerFactory extends DefaultCommandHandlerFactory {

    private BrokerContext brokerContext;
    private List<JMQ2CommandHandler> commandHandlers;

    public JMQ2CommandHandlerFactory(BrokerContext brokerContext) {
        this.brokerContext = brokerContext;
        this.commandHandlers = loadCommandHandlers();
        initCommandHandlers(commandHandlers);
        registerCommandHandlers(commandHandlers);
    }

    protected List<JMQ2CommandHandler> loadCommandHandlers() {
        return Lists.newArrayList(ExtensionManager.getOrLoadExtensions(JMQ2CommandHandler.class));
    }

    protected void initCommandHandlers(List<JMQ2CommandHandler> commandHandlers) {
        for (JMQ2CommandHandler commandHandler : commandHandlers) {
            if (commandHandler instanceof BrokerContextAware) {
                ((BrokerContextAware) commandHandler).setBrokerContext(brokerContext);
            }
        }
    }

    protected void registerCommandHandlers(List<JMQ2CommandHandler> commandHandlers) {
        for (JMQ2CommandHandler commandHandler : commandHandlers) {
            super.register(commandHandler);
        }
    }
}