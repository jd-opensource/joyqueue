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
package org.joyqueue.nsr.message.support;

import com.google.common.collect.Lists;
import org.joyqueue.domain.Broker;
import org.joyqueue.event.MetaEvent;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.network.command.BooleanAck;
import org.joyqueue.network.transport.TransportServer;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.CommandCallback;
import org.joyqueue.network.transport.command.JoyQueueCommand;
import org.joyqueue.network.transport.exception.TransportException;
import org.joyqueue.nsr.config.MessengerConfig;
import org.joyqueue.nsr.exception.MessengerException;
import org.joyqueue.nsr.message.MessageListener;
import org.joyqueue.nsr.message.Messenger;
import org.joyqueue.nsr.message.support.network.command.MessengerPublishRequest;
import org.joyqueue.nsr.message.support.network.transport.MessengerTransportServerFactory;
import org.joyqueue.nsr.message.support.session.MessengerSessionManager;
import org.joyqueue.toolkit.concurrent.EventBus;
import org.joyqueue.toolkit.config.PropertySupplier;
import org.joyqueue.toolkit.config.PropertySupplierAware;
import org.joyqueue.toolkit.service.Service;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * DefaultMessenger
 * author: gaohaoxiang
 * date: 2019/8/27
 */
public class DefaultMessenger extends Service implements Messenger<MetaEvent>, PropertySupplierAware {

    protected static final Logger logger = LoggerFactory.getLogger(DefaultMessenger.class);

    private final EventBus eventBus = new EventBus("joyqueue-messenger-eventBus");

    private MessengerConfig config;
    private MessengerSessionManager messengerSessionManager;
    private TransportServer messengerTransportServer;

    @Override
    protected void validate() throws Exception {
        messengerSessionManager = new MessengerSessionManager(config);
    }

    @Override
    protected void doStart() throws Exception {
        config.getServerConfig().setIoThread(1);
        config.getServerConfig().setPort(config.getPort());
        config.getServerConfig().setIoThreadName("joyqueue-messenger-io-eventLoop");
        config.getServerConfig().setAcceptThreadName("joyqueue-messenger-accept-eventLoop");
        messengerTransportServer = new MessengerTransportServerFactory(config, eventBus).bind(config.getServerConfig());
        messengerTransportServer.start();
        messengerSessionManager.start();
        eventBus.start();
    }

    @Override
    protected void doStop() {
        messengerSessionManager.stop();
        messengerTransportServer.stop();
        eventBus.stop();
    }

    @Override
    public void publish(MetaEvent event, List<Broker> brokers) {
        if (!config.getPublishEnable() || CollectionUtils.isEmpty(brokers)) {
            return;
        }

        boolean[] success = {true};
        CountDownLatch latch = new CountDownLatch(brokers.size());

        for (Broker broker : brokers) {
            MessengerPublishRequest messengerPublishRequest = new MessengerPublishRequest(event);
            try {
                messengerSessionManager.getOrCreateSession(broker.getId(), broker.getIp(), broker.getMessengerPort())
                        .async(new JoyQueueCommand(messengerPublishRequest), config.getSessionTimeout(), new CommandCallback() {
                            @Override
                            public void onSuccess(Command request, Command response) {
                                BooleanAck booleanAck = (BooleanAck) response.getPayload();

                                if (booleanAck.getHeader().getStatus() != JoyQueueCode.SUCCESS.getCode()) {
                                    logger.warn("messenger publish error, event: {}, id: {}, ip: {}, port: {}, code: {}",
                                            event, broker.getId(), broker.getIp(), broker.getMessengerPort(), booleanAck.type());

                                    success[0] = false;
                                }
                                latch.countDown();
                            }

                            @Override
                            public void onException(Command request, Throwable cause) {
                                logger.warn("messenger publish error, event: {}, id: {}, ip: {}, port: {}",
                                        event, broker.getId(), broker.getIp(), broker.getMessengerPort(), cause);

                                boolean isSuccess = false;
                                if (cause instanceof TransportException.RequestErrorException) {
                                    if (config.getPublishIgnoreConnectionError()) {
                                        isSuccess = true;
                                    }
                                }

                                success[0] = isSuccess;
                                latch.countDown();
                            }
                        });
            } catch (Exception e) {
                if (!config.getPublishIgnoreConnectionError()) {
                    success[0] = false;
                    logger.warn("create session exception, event: {}, brokerId: {}, brokerIp: {}, brokerPort: {}",
                            event, broker.getId(), broker.getIp(), broker.getMessengerPort(), e);
                }
                latch.countDown();
            }
        }

        try {
            if (!latch.await(config.getPublishTimeout(), TimeUnit.MILLISECONDS)) {
                logger.warn("messenger publish timeout, event: {}, brokers: {}, timeout: {}",
                        event, brokers, config.getPublishTimeout());

                if (!config.getPublishForce()) {
                    throw new MessengerException("messenger publish timeout");
                }
            }
        } catch (InterruptedException e) {
            throw new MessengerException("messenger publish exception", e);
        }

        if (!success[0]) {
            if (!config.getPublishForce()) {
                logger.warn("messenger publish failed, event: {}, brokers: {}", event, brokers);
                throw new MessengerException("messenger publish failed");
            }
        }
    }

    @Override
    public void publish(MetaEvent event, Broker... brokers) {
        publish(event, Lists.newArrayList(brokers));
    }

    @Override
    public void fastPublish(MetaEvent event, List<Broker> brokers) {
        if (CollectionUtils.isEmpty(brokers)) {
            return;
        }

        for (Broker broker : brokers) {
            try {
                MessengerPublishRequest messengerPublishRequest = new MessengerPublishRequest(event);
                messengerSessionManager.getOrCreateSession(broker.getId(), broker.getIp(), broker.getMessengerPort())
                        .oneway(new JoyQueueCommand(messengerPublishRequest), config.getSessionTimeout());
            } catch (Exception e) {
                logger.warn("messenger fastPublish failed, event: {}, broker: {}", event, broker);
            }
        }
    }

    @Override
    public void fastPublish(MetaEvent event, Broker... brokers) {
        fastPublish(event, Lists.newArrayList(brokers));
    }

    @Override
    public void addListener(MessageListener listener) {
        eventBus.addListener(listener);
    }

    @Override
    public void setSupplier(PropertySupplier supplier) {
        this.config = new MessengerConfig(supplier);
    }

    @Override
    public String type() {
        return "default";
    }
}