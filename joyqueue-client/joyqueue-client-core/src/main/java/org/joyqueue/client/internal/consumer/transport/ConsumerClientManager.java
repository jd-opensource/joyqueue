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
package org.joyqueue.client.internal.consumer.transport;

import org.joyqueue.client.internal.nameserver.NameServerConfig;
import org.joyqueue.client.internal.transport.Client;
import org.joyqueue.client.internal.transport.ClientGroup;
import org.joyqueue.client.internal.transport.ClientManager;
import org.joyqueue.client.internal.transport.config.TransportConfig;
import org.joyqueue.network.domain.BrokerNode;
import org.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ConsumerClientManager
 *
 * author: gaohaoxiang
 * date: 2018/11/28
 */
public class ConsumerClientManager extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(ConsumerClientManager.class);

    private TransportConfig transportConfig;
    private NameServerConfig nameServerConfig;

    private ClientManager clientManager;

    public ConsumerClientManager(TransportConfig transportConfig, NameServerConfig nameServerConfig) {
        this.transportConfig = transportConfig;
        this.nameServerConfig = nameServerConfig;
    }

    public ConsumerClientGroup getClientGroup(BrokerNode node) {
        ClientGroup clientGroup = clientManager.getClientGroup(node);
        if (clientGroup == null) {
            return null;
        }
        return new ConsumerClientGroup(clientGroup);
    }

    public ConsumerClient createClient(BrokerNode node) {
        Client client = clientManager.createClient(node);
        return new ConsumerClient(client);
    }

    public ConsumerClient getOrCreateClient(BrokerNode node) {
        Client client = clientManager.getOrCreateClient(node);
        return ConsumerClient.build(client);
    }

    public ConsumerClient getClient(BrokerNode node) {
        Client client = clientManager.getClient(node);
        if (client == null) {
            return null;
        }
        return ConsumerClient.build(client);
    }

    public ConsumerClient tryGetClient(BrokerNode node) {
        Client client = clientManager.tryGetClient(node);
        if (client == null) {
            return null;
        }
        return ConsumerClient.build(client);
    }

    public void closeClient(BrokerNode node) {
        ConsumerClient consumerClient = tryGetClient(node);
        if (consumerClient == null) {
            return;
        }
        consumerClient.close();
        clientManager.closeClient(node);
    }

    @Override
    protected void validate() throws Exception {
        clientManager = new ClientManager(transportConfig, nameServerConfig);
    }

    @Override
    protected void doStart() throws Exception {
        clientManager.start();
    }

    @Override
    protected void doStop() {
        if (clientManager != null) {
            clientManager.stop();
        }
    }
}