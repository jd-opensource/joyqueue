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
package org.joyqueue.broker.kafka.network.protocol;

import com.google.common.collect.Lists;
import org.joyqueue.broker.BrokerContextAware;
import org.joyqueue.broker.kafka.KafkaCommandHandler;
import org.joyqueue.broker.kafka.KafkaContext;
import org.joyqueue.broker.kafka.KafkaContextAware;
import org.joyqueue.network.transport.command.handler.CommandHandler;
import org.joyqueue.network.transport.command.support.DefaultCommandHandlerFactory;
import com.jd.laf.extension.ExtensionManager;

import java.util.List;

/**
 * KafkaCommandHandlerFactory
 *
 * author: gaohaoxiang
 * date: 2018/11/14
 */
public class KafkaCommandHandlerFactory extends DefaultCommandHandlerFactory {

    private KafkaContext kafkaContext;
    private List<KafkaCommandHandler> commandHandlers;

    public KafkaCommandHandlerFactory(KafkaContext kafkaContext) {
        this.kafkaContext = kafkaContext;
        this.commandHandlers = loadCommandHandlers();
        initCommandHandlers(commandHandlers);
        registerCommandHandlers(commandHandlers);
    }

    protected List<KafkaCommandHandler> loadCommandHandlers() {
        return Lists.newArrayList(ExtensionManager.getOrLoadExtensions(KafkaCommandHandler.class));
    }

    protected void initCommandHandlers(List<KafkaCommandHandler> commandHandlers) {
        for (KafkaCommandHandler commandHandler : commandHandlers) {
            if (commandHandler instanceof BrokerContextAware) {
                ((BrokerContextAware) commandHandler).setBrokerContext(kafkaContext.getBrokerContext());
            }
            if (commandHandler instanceof KafkaContextAware) {
                ((KafkaContextAware) commandHandler).setKafkaContext(kafkaContext);
            }
        }
    }

    protected void registerCommandHandlers(List<KafkaCommandHandler> commandHandlers) {
        for (KafkaCommandHandler commandHandler : commandHandlers) {
            register(commandHandler);
        }
    }

    @Override
    public void register(CommandHandler commandHandler) {
        super.register(commandHandler);
    }

    @Override
    public void register(int type, CommandHandler commandHandler) {
        super.register(type, commandHandler);
    }
}