package org.joyqueue.broker.network.codec;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.buffer.ByteBuf;
import org.joyqueue.broker.network.command.GetPartitionGroupClusterRequest;
import org.joyqueue.network.command.CommandType;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.codec.JoyQueueHeader;
import org.joyqueue.network.transport.command.Type;

import java.util.List;
import java.util.Map;

/**
 * GetPartitionGroupClusterRequestCodec
 * author: gaohaoxiang
 * date: 2020/3/20
 */
public class GetPartitionGroupClusterRequestCodec implements Type, BrokerPayloadCodec<GetPartitionGroupClusterRequest> {

    @Override
    public Object decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        GetPartitionGroupClusterRequest request = new GetPartitionGroupClusterRequest();
        Map<String, List<Integer>> groups = Maps.newHashMap();
        int topicSize = buffer.readInt();
        for (int i = 0; i < topicSize; i++) {
            String topic = Serializer.readString(buffer);
            int partitionGroupSize = buffer.readInt();
            List<Integer> partitionGroups = Lists.newArrayListWithCapacity(partitionGroupSize);
            for (int j = 0; j < partitionGroupSize; j++) {
                int group = buffer.readInt();
                partitionGroups.add(group);
            }
            groups.put(topic, partitionGroups);
        }
        request.setGroups(groups);
        return request;
    }

    @Override
    public void encode(GetPartitionGroupClusterRequest payload, ByteBuf buffer) throws Exception {
        buffer.writeInt(payload.getGroups().size());
        for (Map.Entry<String, List<Integer>> entry : payload.getGroups().entrySet()) {
            Serializer.write(entry.getKey(), buffer);
            buffer.writeInt(entry.getValue().size());
            for (Integer partitionGroup : entry.getValue()) {
                buffer.writeInt(partitionGroup);
            }
        }
    }

    @Override
    public int type() {
        return CommandType.GET_PARTITION_GROUP_CLUSTER_REQUEST;
    }
}