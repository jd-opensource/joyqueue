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
package org.joyqueue.client.internal.transport;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.Lists;
import org.joyqueue.client.internal.transport.config.TransportConfig;
import org.joyqueue.network.domain.BrokerNode;
import org.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * ClientGroupManager
 *
 * author: gaohaoxiang
 * date: 2018/11/29
 */
public class ClientGroupManager extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(ClientGroupManager.class);

    private TransportConfig config;
    private Cache<BrokerNode, ClientGroup> clientGroupCache;

    public ClientGroupManager(TransportConfig config) {
        this.config = config;
    }

    public ClientGroup tryGetClientGroup(BrokerNode node) {
        return clientGroupCache.asMap().get(node);
    }

    public ClientGroup getClientGroup(BrokerNode node) {
        return clientGroupCache.getIfPresent(node);
    }

    public ClientGroup getClientGroup(BrokerNode node, final Callable<ClientGroup> callable) throws Exception {
        return clientGroupCache.get(node, new Callable<ClientGroup>() {
            @Override
            public ClientGroup call() throws Exception {
                return callable.call();
            }
        });
    }

    public void closeClientGroup(ClientGroup clientGroup) {
        clientGroupCache.invalidate(clientGroup.getNode());
    }

    public void closeClientGroup(BrokerNode node) {
        clientGroupCache.invalidate(node);
    }

    public List<ClientGroup> getGroups() {
        return Lists.newArrayList(clientGroupCache.asMap().values());
    }

    @Override
    protected void validate() throws Exception {
        clientGroupCache = CacheBuilder.newBuilder()
                .expireAfterAccess(config.getChannelMaxIdleTime(), TimeUnit.MILLISECONDS)
                .removalListener(new RemovalListener<BrokerNode, ClientGroup>() {
                    @Override
                    public void onRemoval(RemovalNotification<BrokerNode, ClientGroup> removalNotification) {
                        try {
                            removalNotification.getValue().stop();
                        } catch (Exception e) {
                            logger.error("close client exception, address: {}, error: {}", removalNotification.getKey().getHost(), e.getMessage());
                            logger.debug("close client exception, address: {}", removalNotification.getKey().getHost(), e);
                        }
                    }
                })
                .build();
    }

    @Override
    protected void doStop() {
        if (clientGroupCache != null) {
            for (Map.Entry<BrokerNode, ClientGroup> entry : clientGroupCache.asMap().entrySet()) {
                entry.getValue().stop();
            }
        }
    }
}