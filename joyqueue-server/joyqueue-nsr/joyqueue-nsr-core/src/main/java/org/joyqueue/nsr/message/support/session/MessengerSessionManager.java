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
package org.joyqueue.nsr.message.support.session;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import org.joyqueue.domain.Broker;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.TransportClient;
import org.joyqueue.network.transport.exception.TransportException;
import org.joyqueue.nsr.config.MessengerConfig;
import org.joyqueue.nsr.message.support.network.transport.MessengerTransportClientFactory;
import org.joyqueue.toolkit.concurrent.NamedThreadFactory;
import org.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * MessengerSessionManager
 * author: gaohaoxiang
 * date: 2019/8/27
 */
public class MessengerSessionManager extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(MessengerSessionManager.class);

    private MessengerConfig config;
    private TransportClient client;
    private Cache<Integer, MessengerSession> sessions;
    private ScheduledExecutorService heartbeatThreadPool;

    public MessengerSessionManager(MessengerConfig config) {
        this.config = config;
    }

    @Override
    protected void validate() throws Exception {
        client = new MessengerTransportClientFactory().create(config.getClientConfig());
        sessions = CacheBuilder.newBuilder()
                .expireAfterAccess(config.getSessionExpireTime(), TimeUnit.MILLISECONDS)
                .removalListener((RemovalNotification<Integer, MessengerSession> notification) -> {
                    try {
                        MessengerSession session = notification.getValue();
                        logger.info("create session, id: {}, ip: {}, port: {}", session.getBrokerId(), session.getBrokerHost(), session.getBrokerHost());
                        session.stop();
                    } catch (Exception e) {
                        logger.error("stop session exception, id: {}", notification.getKey(), e);
                    }
                })
                .build();
    }

    @Override
    public void doStop() {
        if (heartbeatThreadPool != null) {
            heartbeatThreadPool.shutdown();
        }
        if (client != null) {
            client.stop();
        }
        if (sessions != null) {
            sessions.cleanUp();
        }
    }

    public MessengerSession getSession(Broker broker) {
        return getSession(broker.getId());
    }

    public MessengerSession getSession(int brokerId) {
        return sessions.getIfPresent(brokerId);
    }

    public MessengerSession getOrCreateSession(Broker broker) {
        return getOrCreateSession(broker.getId(), broker.getIp(), broker.getBackEndPort());
    }

    public MessengerSession getOrCreateSession(int brokerId, String brokerHost, int brokerPort) throws TransportException {
        maybeInitHeartbeat();
        try {
            return sessions.get(brokerId, () -> {
                Transport transport = client.createTransport(new InetSocketAddress(brokerHost, brokerPort));
                logger.info("create session, id: {}, ip: {}, port: {}", brokerId, brokerHost, brokerPort);
                return new MessengerSession(brokerId, brokerHost, brokerPort, config, transport);
            });
        } catch (ExecutionException e) {
            throw new TransportException.ConnectionException(String.format("create session failed, broker: {id: %s, ip: %s, port: %s}",
                    brokerId, brokerHost, brokerPort), e.getCause());
        }
    }

    protected void maybeInitHeartbeat() {
        if (heartbeatThreadPool != null) {
            return;
        }
        synchronized (this) {
            if (heartbeatThreadPool != null) {
                return;
            }

            heartbeatThreadPool = Executors.newScheduledThreadPool(1, new NamedThreadFactory("joyqueue-messenger-heartbeat"));
            heartbeatThreadPool.scheduleWithFixedDelay(new MessengerSessionHeartbeatThread(config, this),
                    config.getHeartbeatInterval(), config.getHeartbeatInterval(), TimeUnit.MILLISECONDS);
        }
    }

    public ConcurrentMap<Integer, MessengerSession> getSessions() {
        return sessions.asMap();
    }
}