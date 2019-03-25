package com.jd.journalq.client.internal.cluster;

import com.jd.journalq.client.internal.nameserver.NameServerConfig;
import com.jd.journalq.client.internal.nameserver.NameServerConfigChecker;
import com.jd.journalq.client.internal.transport.Client;
import com.jd.journalq.client.internal.transport.ClientManager;
import com.jd.journalq.client.internal.transport.config.TransportConfig;
import com.jd.journalq.network.domain.BrokerNode;
import com.jd.journalq.toolkit.URL;
import com.jd.journalq.toolkit.service.Service;

/**
 * ClusterClientManager
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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

        URL url = URL.valueOf(String.format("jmq://%s", nameServerConfig.getAddress()));
        bootstrapNode = new BrokerNode(url.getHost(), url.getPort());
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