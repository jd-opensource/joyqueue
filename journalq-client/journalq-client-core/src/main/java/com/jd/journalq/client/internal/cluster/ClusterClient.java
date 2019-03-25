package com.jd.journalq.client.internal.cluster;

import com.jd.journalq.client.internal.nameserver.NameServerConfig;
import com.jd.journalq.client.internal.transport.Client;
import com.jd.journalq.client.internal.transport.ClientState;
import com.jd.journalq.client.internal.transport.config.TransportConfig;
import com.jd.journalq.common.network.command.FetchAssignedPartition;
import com.jd.journalq.common.network.command.FetchAssignedPartitionAck;
import com.jd.journalq.common.network.command.FetchAssignedPartitionData;
import com.jd.journalq.common.network.command.FetchCluster;
import com.jd.journalq.common.network.command.FetchClusterAck;
import com.jd.journalq.common.network.command.FindCoordinator;
import com.jd.journalq.common.network.command.FindCoordinatorAck;
import com.jd.journalq.common.network.transport.command.Command;
import com.jd.journalq.common.network.transport.command.JMQCommand;

import java.util.List;

/**
 * ProducerClient
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/29
 */
public class ClusterClient {

    private static final String CLIENT_CLUSTER_CACHE_KEY = "_CLIENT_CLUSTER_CACHE_";

    private Client client;
    private TransportConfig transportConfig;
    private NameServerConfig nameServerConfig;

    public static ClusterClient build(Client client, TransportConfig transportConfig, NameServerConfig nameServerConfig) {
        ClusterClient clusterClient = client.getAttribute().get(CLIENT_CLUSTER_CACHE_KEY);
        if (clusterClient == null) {
            clusterClient = new ClusterClient(client, transportConfig, nameServerConfig);
            ClusterClient oldClusterClient = client.getAttribute().putIfAbsent(CLIENT_CLUSTER_CACHE_KEY, clusterClient);
            if (oldClusterClient != null) {
                clusterClient = oldClusterClient;
            }
        }
        return clusterClient;
    }

    public ClusterClient(Client client, TransportConfig transportConfig, NameServerConfig nameServerConfig) {
        this.client = client;
        this.transportConfig = transportConfig;
        this.nameServerConfig = nameServerConfig;
    }

    public FetchAssignedPartitionAck fetchAssignedPartition(List<FetchAssignedPartitionData> data, String app) {
        FetchAssignedPartition fetchAssignedPartition = new FetchAssignedPartition();
        fetchAssignedPartition.setApp(app);
        fetchAssignedPartition.setData(data);

        Command response = client.sync(new JMQCommand(fetchAssignedPartition));
        return (FetchAssignedPartitionAck) response.getPayload();
    }

    public FindCoordinatorAck findCoordinators(List<String> topics, String app) {
        FindCoordinator findCoordinator = new FindCoordinator();
        findCoordinator.setTopics(topics);
        findCoordinator.setApp(app);

        Command response = client.sync(new JMQCommand(findCoordinator));
        return (FindCoordinatorAck) response.getPayload();
    }

    public FetchClusterAck fetchCluster(List<String> topics, String app) {
        FetchCluster fetchCluster = new FetchCluster();
        fetchCluster.setTopics(topics);
        fetchCluster.setApp(app);

        Command response = client.sync(new JMQCommand(fetchCluster));
        return (FetchClusterAck) response.getPayload();
    }

    public Client getClient() {
        return client;
    }

    public ClientState getState() {
        return client.getState();
    }

    public void close() {
        client.stop();
    }
}