package org.joyqueue.broker.network.codec;

import io.netty.buffer.ByteBuf;
import org.apache.commons.collections.MapUtils;
import org.joyqueue.broker.network.command.GetPartitionGroupClusterResponse;
import org.joyqueue.network.command.CommandType;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.codec.JoyQueueHeader;
import org.joyqueue.network.transport.command.Type;

import java.util.Map;

/**
 * GetPartitionGroupClusterResponseCodec
 * author: gaohaoxiang
 * date: 2020/3/20
 */
public class GetPartitionGroupClusterResponseCodec implements Type, BrokerPayloadCodec<GetPartitionGroupClusterResponse> {

    @Override
    public Object decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        GetPartitionGroupClusterResponse response = new GetPartitionGroupClusterResponse();
        int topicSize = buffer.readInt();
        for (int i = 0; i < topicSize; i++) {
            String topic = Serializer.readString(buffer);
            int clusterSize = buffer.readInt();
            for (int j = 0; j < clusterSize; j++) {
                GetPartitionGroupClusterResponse.PartitionGroupCluster cluster = new GetPartitionGroupClusterResponse.PartitionGroupCluster();
                int group = buffer.readInt();
                int nodeSize = buffer.readInt();
                for (int k = 0; k < nodeSize; k++) {
                    GetPartitionGroupClusterResponse.PartitionGroupNode node = new GetPartitionGroupClusterResponse.PartitionGroupNode();
                    node.setId(buffer.readInt());
                    node.setWritable(buffer.readBoolean());
                    node.setReadable(buffer.readBoolean());
                    cluster.addNode(node);
                }
                response.addCluster(topic, group, cluster);
            }
        }
        return response;
    }

    @Override
    public void encode(GetPartitionGroupClusterResponse payload, ByteBuf buffer) throws Exception {
        if (MapUtils.isEmpty(payload.getGroups())) {
            buffer.writeInt(0);
        } else {
            buffer.writeInt(payload.getGroups().size());
            for (Map.Entry<String, Map<Integer, GetPartitionGroupClusterResponse.PartitionGroupCluster>> entry : payload.getGroups().entrySet()) {
                Map<Integer, GetPartitionGroupClusterResponse.PartitionGroupCluster> clusterMap = entry.getValue();
                Serializer.write(entry.getKey(), buffer);
                buffer.writeInt(clusterMap.size());
                for (Map.Entry<Integer, GetPartitionGroupClusterResponse.PartitionGroupCluster> clusterEntry : clusterMap.entrySet()) {
                    GetPartitionGroupClusterResponse.PartitionGroupCluster cluster = clusterEntry.getValue();
                    buffer.writeInt(clusterEntry.getKey());
                    buffer.writeInt(cluster.getNodes().size());
                    for (GetPartitionGroupClusterResponse.PartitionGroupNode node : cluster.getNodes()) {
                        buffer.writeInt(node.getId());
                        buffer.writeBoolean(node.isWritable());
                        buffer.writeBoolean(node.isReadable());
                    }
                }
            }
        }
    }

    @Override
    public int type() {
        return CommandType.GET_PARTITION_GROUP_CLUSTER_RESPONSE;
    }
}