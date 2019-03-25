package com.jd.journalq.client.internal.consumer.transport;

import com.jd.journalq.client.internal.nameserver.NameServerConfig;
import com.jd.journalq.client.internal.transport.Client;
import com.jd.journalq.client.internal.transport.ClientGroup;
import com.jd.journalq.client.internal.transport.ClientManager;
import com.jd.journalq.client.internal.transport.config.TransportConfig;
import com.jd.journalq.common.network.domain.BrokerNode;
import com.jd.journalq.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ConsumerClientManager
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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