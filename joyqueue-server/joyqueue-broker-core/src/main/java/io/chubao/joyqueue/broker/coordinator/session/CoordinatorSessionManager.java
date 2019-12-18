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
package io.chubao.joyqueue.broker.coordinator.session;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import io.chubao.joyqueue.broker.coordinator.config.CoordinatorConfig;
import io.chubao.joyqueue.broker.network.support.BrokerTransportClientFactory;
import io.chubao.joyqueue.domain.Broker;
import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.TransportClient;
import io.chubao.joyqueue.network.transport.config.ClientConfig;
import io.chubao.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * CoordinatorSessionManager
 *
 * author: gaohaoxiang
 * date: 2018/11/9
 */
public class CoordinatorSessionManager extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(CoordinatorSessionManager.class);

    private CoordinatorConfig config;
    private TransportClient client;
    private Cache<Integer, CoordinatorSession> sessions;

    public CoordinatorSessionManager(CoordinatorConfig config) {
        this.config = config;
    }

    @Override
    protected void validate() throws Exception {
        client = new BrokerTransportClientFactory().create(new ClientConfig());
        sessions = CacheBuilder.newBuilder()
                .expireAfterAccess(config.getSessionExpireTime(), TimeUnit.MILLISECONDS)
                .removalListener((RemovalNotification<Integer, CoordinatorSession>  notification) -> {
                    try {
                        CoordinatorSession session = notification.getValue();
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
        if (client != null) {
            client.stop();
        }
        if (sessions != null) {
            sessions.cleanUp();
        }
    }

    public CoordinatorSession getSession(Broker broker) {
        return getSession(broker.getId());
    }

    public CoordinatorSession getSession(int brokerId) {
        return sessions.getIfPresent(brokerId);
    }

    public CoordinatorSession getOrCreateSession(Broker broker) {
        return getOrCreateSession(broker.getId(), broker.getIp(), broker.getBackEndPort());
    }

    public CoordinatorSession getOrCreateSession(int brokerId, String brokerHost, int brokerPort) {
        try {
            return sessions.get(brokerId, () -> {
                logger.info("create session, id: {}, ip: {}, port: {}", brokerId, brokerHost, brokerPort);
                Transport transport = client.createTransport(new InetSocketAddress(brokerHost, brokerPort));
                return new CoordinatorSession(brokerId, brokerHost, brokerPort, config, transport);
            });
        } catch (ExecutionException e) {
            throw new RuntimeException(String.format("create session failed, broker: {id: %s, ip: %s, port: %s}",
                    brokerId, brokerHost, brokerPort), e);
        }
    }
}