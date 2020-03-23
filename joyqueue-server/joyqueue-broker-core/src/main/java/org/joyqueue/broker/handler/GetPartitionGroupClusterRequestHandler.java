package org.joyqueue.broker.handler;

import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.BrokerContextAware;
import org.joyqueue.broker.network.BrokerCommandHandler;
import org.joyqueue.broker.network.command.GetPartitionGroupClusterRequest;
import org.joyqueue.broker.network.command.GetPartitionGroupClusterResponse;
import org.joyqueue.network.command.CommandType;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.store.StoreNode;
import org.joyqueue.store.StoreNodes;
import org.joyqueue.store.StoreService;

import java.util.Map;

/**
 * GetPartitionGroupClusterRequestHandler
 * author: gaohaoxiang
 * date: 2020/3/19
 */
public class GetPartitionGroupClusterRequestHandler implements Type, BrokerCommandHandler, BrokerContextAware {

    private StoreService storeService;

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.storeService = brokerContext.getStoreService();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        GetPartitionGroupClusterRequest request = (GetPartitionGroupClusterRequest) command.getPayload();
        GetPartitionGroupClusterResponse response = new GetPartitionGroupClusterResponse();
        for (Map.Entry<String, Integer> entry : request.getGroups().entrySet()) {
            StoreNodes nodes = storeService.getNodes(entry.getKey(), entry.getValue());
            if (nodes == null) {
                continue;
            }
            GetPartitionGroupClusterResponse.PartitionGroupCluster partitionGroupCluster = new GetPartitionGroupClusterResponse.PartitionGroupCluster();
            for (StoreNode node : nodes.getNodes()) {
                partitionGroupCluster.addNode(new GetPartitionGroupClusterResponse.PartitionGroupNode(node.getId(), node.isWritable(), node.isReadable()));
            }
            response.addCluster(entry.getKey(), entry.getValue(), partitionGroupCluster);
        }
        return new Command(response);
    }

    @Override
    public int type() {
        return CommandType.GET_PARTITION_GROUP_CLUSTER_REQUEST;
    }
}