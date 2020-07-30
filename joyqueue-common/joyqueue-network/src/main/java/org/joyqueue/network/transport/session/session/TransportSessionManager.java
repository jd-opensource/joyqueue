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
package org.joyqueue.network.transport.session.session;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import org.joyqueue.domain.Broker;
import org.joyqueue.network.transport.TransportClient;
import org.joyqueue.network.transport.TransportClientFactory;
import org.joyqueue.network.transport.config.ClientConfig;
import org.joyqueue.network.transport.exception.TransportException;
import org.joyqueue.network.transport.session.session.config.TransportSessionConfig;
import org.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * TransportSessionManager
 *
 * author: gaohaoxiang
 * date: 2018/11/9
 */
public class TransportSessionManager extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(TransportSessionManager.class);

    private TransportSessionConfig config;
    private TransportClientFactory transportClientFactory;
    private ClientConfig clientConfig;
    private TransportClient client;
    private Cache<String, TransportSession> sessions;

    public TransportSessionManager(TransportSessionConfig config, ClientConfig clientConfig, TransportClientFactory transportClientFactory) {
        this.config = config;
        this.clientConfig = clientConfig;
        this.transportClientFactory = transportClientFactory;
    }

    @Override
    protected void validate() throws Exception {
        client = transportClientFactory.create(clientConfig);
        sessions = CacheBuilder.newBuilder()
                .expireAfterAccess(config.getSessionExpireTime(), TimeUnit.MILLISECONDS)
                .removalListener((RemovalNotification<String, TransportSession>  notification) -> {
                    try {
                        TransportSession session = notification.getValue();
                        logger.info("create session, id: {}, ip: {}, port: {}", session.getId(), session.getHost(), session.getPort());
                        session.stop();
                    } catch (Exception e) {
                        logger.error("stop session exception, id: {}", notification.getKey(), e);
                    }
                })
                .build();
    }

    @Override
    public void doStop() {
        if (client != null) {
            client.stop();
        }
        if (sessions != null) {
            sessions.cleanUp();
        }
    }

    public TransportSession getOrCreateSession(Broker broker) {
        return getOrCreateSession(broker.getId(), broker.getIp(), broker.getBackEndPort());
    }

    public TransportSession getOrCreateSession(int brokerId, String brokerHost, int brokerPort) {
        try {
            return sessions.get(generateId(brokerId, brokerHost, brokerPort), () -> {
                logger.info("create session, id: {}, ip: {}, port: {}", brokerId, brokerHost, brokerPort);
                return new TransportSession(brokerId, brokerHost, brokerPort, clientConfig, config, client);
            });
        } catch (ExecutionException e) {
            throw new TransportException.ConnectionException(String.format("create session failed, broker: {id: %s, ip: %s, port: %s}",
                    brokerId, brokerHost, brokerPort), e);
        }
    }

    protected String generateId(int brokerId, String brokerHost, int brokerPort) {
        return brokerHost + ":" + brokerPort;
    }
}