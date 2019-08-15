package io.chubao.joyqueue.client.internal.producer.transport;

import io.chubao.joyqueue.client.internal.nameserver.NameServerConfig;
import io.chubao.joyqueue.client.internal.transport.Client;
import io.chubao.joyqueue.client.internal.transport.ClientGroup;
import io.chubao.joyqueue.client.internal.transport.ClientManager;
import io.chubao.joyqueue.client.internal.transport.config.TransportConfig;
import io.chubao.joyqueue.network.domain.BrokerNode;
import io.chubao.joyqueue.toolkit.retry.RetryPolicy;
import io.chubao.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ProducerClientManager
 *
 * author: gaohaoxiang
 * date: 2018/11/28
 */
public class ProducerClientManager extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(ProducerClientManager.class);

    private TransportConfig transportConfig;
    private NameServerConfig nameServerConfig;

    private ClientManager clientManager;

    public ProducerClientManager(TransportConfig transportConfig, NameServerConfig nameServerConfig) {
        this.transportConfig = transportConfig;
        this.nameServerConfig = nameServerConfig;
    }

    public ProducerClientGroup getClientGroup(BrokerNode node) {
        ClientGroup clientGroup = clientManager.getClientGroup(node);
        if (clientGroup == null) {
            return null;
        }
        return new ProducerClientGroup(clientGroup);
    }

    public ProducerClient createClient(BrokerNode node) {
        Client client = clientManager.createClient(node);
        return new ProducerClient(client);
    }

    public ProducerClient getOrCreateClient(BrokerNode node) {
        Client client = clientManager.getOrCreateClient(node);
        return ProducerClient.build(client);
    }

    public ProducerClient getClient(BrokerNode node) {
        Client client = clientManager.getClient(node);
        if (client == null) {
            return null;
        }
        return ProducerClient.build(client);
    }

    public ProducerClient tryGetClient(BrokerNode node) {
        Client client = clientManager.tryGetClient(node);
        if (client == null) {
            return null;
        }
        return ProducerClient.build(client);
    }

    public void closeClient(BrokerNode node) {
        ProducerClient producerClient = tryGetClient(node);
        if (producerClient == null) {
            return;
        }
        producerClient.close();
        clientManager.closeClient(node);
    }

    @Override
    protected void validate() throws Exception {
        transportConfig = transportConfig.copy();
        transportConfig.setRetryPolicy(new RetryPolicy(0, 0));
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