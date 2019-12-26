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
package org.joyqueue.nsr.message.support.network.handler;

import com.alibaba.fastjson.JSON;
import org.joyqueue.event.MetaEvent;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.network.command.BooleanAck;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.network.transport.command.handler.CommandHandler;
import org.joyqueue.network.transport.command.provider.ExecutorServiceProvider;
import org.joyqueue.nsr.config.MessengerConfig;
import org.joyqueue.nsr.message.support.network.command.MessengerPublishRequest;
import org.joyqueue.nsr.network.command.NsrCommandType;
import org.joyqueue.toolkit.concurrent.EventBus;
import org.joyqueue.toolkit.concurrent.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * MessengerPublishRequestHandler
 * author: gaohaoxiang
 * date: 2019/8/27
 */
public class MessengerPublishRequestHandler implements CommandHandler, Type, ExecutorServiceProvider {

    protected static final Logger logger = LoggerFactory.getLogger(MessengerPublishRequestHandler.class);

    private MessengerConfig config;
    private EventBus eventBus;
    private ExecutorService threadPool;

    public MessengerPublishRequestHandler(MessengerConfig config, EventBus eventBus) {
        this.config = config;
        this.eventBus = eventBus;
        this.threadPool = new ThreadPoolExecutor(config.getHandlerThreads(), config.getHandlerThreads(),
                config.getHandlerKeepalive(), TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(config.getHandlerQueues()), new NamedThreadFactory("joyqueue-messenger-handler"));
    }

    @Override
    public Command handle(Transport transport, Command command) {
        MessengerPublishRequest payload = (MessengerPublishRequest) command.getPayload();
        MetaEvent event = payload.getEvent();

        try {
            if (logger.isDebugEnabled()) {
                logger.debug("onEvent, event: {}", JSON.toJSONString(event));
            }

            eventBus.inform(event);
            return BooleanAck.build();
        } catch (Exception e) {
            logger.error("messenger inform exception, event: {}", event, e);
            return BooleanAck.build(JoyQueueCode.CN_UNKNOWN_ERROR);
        }
    }

    @Override
    public ExecutorService getExecutorService(Transport transport, Command command) {
        return threadPool;
    }

    @Override
    public int type() {
        return NsrCommandType.NSR_MESSENGER_PUBLISH_REQUEST;
    }
}