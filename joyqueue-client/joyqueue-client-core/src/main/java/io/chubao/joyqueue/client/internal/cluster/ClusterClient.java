package io.chubao.joyqueue.client.internal.cluster;

import io.chubao.joyqueue.client.internal.nameserver.NameServerConfig;
import io.chubao.joyqueue.client.internal.transport.Client;
import io.chubao.joyqueue.client.internal.transport.ClientState;
import io.chubao.joyqueue.client.internal.transport.config.TransportConfig;
import io.chubao.joyqueue.network.command.FetchAssignedPartitionRequest;
import io.chubao.joyqueue.network.command.FetchAssignedPartitionResponse;
import io.chubao.joyqueue.network.command.FetchAssignedPartitionData;
import io.chubao.joyqueue.network.command.FetchClusterRequest;
import io.chubao.joyqueue.network.command.FetchClusterResponse;
import io.chubao.joyqueue.network.command.FindCoordinatorRequest;
import io.chubao.joyqueue.network.command.FindCoordinatorResponse;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.JoyQueueCommand;

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

    public FetchAssignedPartitionResponse fetchAssignedPartition(List<FetchAssignedPartitionData> data, String app) {
        FetchAssignedPartitionRequest fetchAssignedPartitionRequest = new FetchAssignedPartitionRequest();
        fetchAssignedPartitionRequest.setApp(app);
        fetchAssignedPartitionRequest.setData(data);

        Command response = client.sync(new JoyQueueCommand(fetchAssignedPartitionRequest));
        return (FetchAssignedPartitionResponse) response.getPayload();
    }

    public FindCoordinatorResponse findCoordinators(List<String> topics, String app) {
        FindCoordinatorRequest findCoordinatorRequest = new FindCoordinatorRequest();
        findCoordinatorRequest.setTopics(topics);
        findCoordinatorRequest.setApp(app);

        Command response = client.sync(new JoyQueueCommand(findCoordinatorRequest));
        return (FindCoordinatorResponse) response.getPayload();
    }

    public FetchClusterResponse fetchCluster(List<String> topics, String app) {
        FetchClusterRequest fetchClusterRequest = new FetchClusterRequest();
        fetchClusterRequest.setTopics(topics);
        fetchClusterRequest.setApp(app);

        Command response = client.sync(new JoyQueueCommand(fetchClusterRequest));
        return (FetchClusterResponse) response.getPayload();
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