package com.jd.journalq.client.internal.transport;

import com.google.common.collect.Lists;
import com.jd.journalq.client.internal.exception.ClientException;
import com.jd.journalq.client.internal.nameserver.NameServerConfig;
import com.jd.journalq.client.internal.transport.config.TransportConfig;
import com.jd.journalq.network.domain.BrokerNode;
import com.jd.journalq.network.transport.TransportClient;
import com.jd.journalq.network.transport.exception.TransportException;
import com.jd.journalq.toolkit.service.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ClientGroup
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/28
 */
public class ClientGroup extends Service {

    private BrokerNode node;
    private TransportConfig transportConfig;
    private TransportClient transportClient;
    private NameServerConfig nameServerConfig;

    private List<Client> clients = Lists.newCopyOnWriteArrayList();
    private AtomicInteger connections = new AtomicInteger();
    private AtomicInteger next = new AtomicInteger();

    public ClientGroup(BrokerNode node, TransportConfig transportConfig, TransportClient transportClient, NameServerConfig nameServerConfig) {
        this.node = node;
        this.transportConfig = transportConfig;
        this.transportClient = transportClient;
        this.nameServerConfig = nameServerConfig;
    }

    public Client getClient() {
        if (connections.get() < transportConfig.getConnections()) {
            if (connections.incrementAndGet() < transportConfig.getConnections()) {
                try {
                    return initClient(node, transportConfig, transportClient);
                } catch (Exception e) {
                    connections.decrementAndGet();
                    if (e instanceof TransportException) {
                        throw (TransportException) e;
                    } else {
                        throw new ClientException(e);
                    }
                }
            }
        }
        return doGetClient();
    }

    public Client doGetClient() {
        if (clients.size() == 1) {
            return clients.get(0);
        }
        return selectClient();
    }

    protected Client selectClient() {
        int index = next.getAndIncrement();
        if (index >= clients.size()) {
            next.set(1);
            index = 0;
        }
        return clients.get(index);
    }

    public Client tryGetClient() {
        return clients.get(0);
    }

    public List<Client> getClients() {
        return clients;
    }

    public BrokerNode getNode() {
        return node;
    }

    protected Client initClient(BrokerNode node, TransportConfig transportConfig, TransportClient transportClient) throws Exception {
        Client client = new Client(node, transportConfig, transportClient, nameServerConfig);
        client.start();
        clients.add(client);
        return client;
    }

    @Override
    protected void doStart() throws Exception {
        initClient(node, transportConfig, transportClient);
    }

    @Override
    protected void doStop() {
        for (Client client : clients) {
            client.stop();
        }
    }
}