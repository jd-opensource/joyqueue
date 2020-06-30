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
package org.joyqueue.broker.protocol.network;

import com.google.common.collect.Lists;
import com.jd.laf.extension.ExtensionPoint;
import com.jd.laf.extension.SpiLoader;
import org.joyqueue.broker.BrokerContextAware;
import org.joyqueue.broker.protocol.JoyQueueCommandHandler;
import org.joyqueue.broker.protocol.JoyQueueContext;
import org.joyqueue.broker.protocol.JoyQueueContextAware;
import org.joyqueue.network.transport.command.support.DefaultCommandHandlerFactory;
import org.joyqueue.plugin.ExtensionPointLazyExt;

import java.util.List;

/**
 * JoyQueueCommandHandlerFactory
 *
 * author: gaohaoxiang
 * date: 2018/11/28
 */
public class JoyQueueCommandHandlerFactory extends DefaultCommandHandlerFactory {

    private JoyQueueContext joyQueueContext;
    private List<JoyQueueCommandHandler> commandHandlers;
    private static ExtensionPoint<JoyQueueCommandHandler,String> joyqueueCommandHandler =new ExtensionPointLazyExt(JoyQueueCommandHandler.class, SpiLoader.INSTANCE, null, null);
    public JoyQueueCommandHandlerFactory(JoyQueueContext joyQueueContext) {
        this.joyQueueContext = joyQueueContext;
        this.commandHandlers = loadCommandHandlers();
        initCommandHandlers(commandHandlers);
        registerCommandHandlers(commandHandlers);
    }

    protected List<JoyQueueCommandHandler> loadCommandHandlers() {
        return Lists.newArrayList(joyqueueCommandHandler.extensions());
    }

    protected void initCommandHandlers(List<JoyQueueCommandHandler> commandHandlers) {
        for (JoyQueueCommandHandler commandHandler : commandHandlers) {
            if (commandHandler instanceof BrokerContextAware) {
                ((BrokerContextAware) commandHandler).setBrokerContext(joyQueueContext.getBrokerContext());
            }
            if (commandHandler instanceof JoyQueueContextAware) {
                ((JoyQueueContextAware) commandHandler).setJoyQueueContext(joyQueueContext);
            }
        }
    }

    protected void registerCommandHandlers(List<JoyQueueCommandHandler> commandHandlers) {
        for (JoyQueueCommandHandler commandHandler : commandHandlers) {
            super.register(commandHandler);
        }
    }
}