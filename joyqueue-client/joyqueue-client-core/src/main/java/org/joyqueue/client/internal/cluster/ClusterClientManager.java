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
package org.joyqueue.client.internal.cluster;

import org.joyqueue.client.internal.nameserver.NameServerConfig;
import org.joyqueue.client.internal.nameserver.NameServerConfigChecker;
import org.joyqueue.client.internal.transport.Client;
import org.joyqueue.client.internal.transport.ClientManager;
import org.joyqueue.client.internal.transport.config.TransportConfig;
import org.joyqueue.network.domain.BrokerNode;
import org.joyqueue.toolkit.URL;
import org.joyqueue.toolkit.service.Service;

/**
 * ClusterClientManager
 *
 * author: gaohaoxiang
 * date: 2018/11/28
 */
public class ClusterClientManager extends Service {

    private TransportConfig transportConfig;
    private NameServerConfig nameServerConfig;

    private BrokerNode bootstrapNode;
    private ClientManager clientManager;

    public ClusterClientManager(TransportConfig transportConfig, NameServerConfig nameServerConfig) {
        NameServerConfigChecker.check(nameServerConfig);

        this.transportConfig = transportConfig;
        this.nameServerConfig = nameServerConfig;
    }

    public ClusterClient getOrCreateClient() {
        return getOrCreateClient(bootstrapNode);
    }

    public ClusterClient doGetOrCreateClient() {
        Client client = clientManager.doGetOrCreateClient(bootstrapNode);
        return ClusterClient.build(client, transportConfig, nameServerConfig);
    }

    public ClusterClient createClient(BrokerNode node) {
        Client client = clientManager.createClient(node);
        return new ClusterClient(client, transportConfig, nameServerConfig);
    }

    public ClusterClient getOrCreateClient(BrokerNode node) {
        Client client = clientManager.getOrCreateClient(node);
        return ClusterClient.build(client, transportConfig, nameServerConfig);
    }

    public ClusterClient getClient(BrokerNode node) {
        Client client = clientManager.getClient(node);
        if (client == null) {
            return null;
        }
        return ClusterClient.build(client, transportConfig, nameServerConfig);
    }

    @Override
    protected void validate() throws Exception {
        transportConfig = transportConfig.copy();
        transportConfig.setConnections(1);
        transportConfig.setIoThreads(1);

        URL url = URL.valueOf(String.format("joyqueue://%s", nameServerConfig.getAddress()));
        if (url.getPort() == 0) {
            bootstrapNode = new BrokerNode(url.getHost(), -1);
        } else {
            bootstrapNode = new BrokerNode(url.getHost(), url.getPort());
        }
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