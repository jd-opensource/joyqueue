package io.chubao.joyqueue.network.codec;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.network.command.FetchAssignedPartitionAckData;
import io.chubao.joyqueue.network.command.FetchAssignedPartitionResponse;
import io.chubao.joyqueue.network.command.JoyQueueCommandType;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.codec.PayloadCodec;
import io.chubao.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.Map;

/**
 * FetchAssignedPartitionResponseCodec
 *
 * author: gaohaoxiang
 * date: 2018/12/4
 */
public class FetchAssignedPartitionResponseCodec implements PayloadCodec<JoyQueueHeader, FetchAssignedPartitionResponse>, Type {

    @Override
    public FetchAssignedPartitionResponse decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        FetchAssignedPartitionResponse fetchAssignedPartitionResponse = new FetchAssignedPartitionResponse();
        Map<String, FetchAssignedPartitionAckData> topicPartitions = Maps.newHashMap();

        short topicSize = buffer.readShort();
        for (int i = 0; i < topicSize; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);

            int partitionSize = buffer.readShort();
            List<Short> partitions = Lists.newArrayListWithCapacity(partitionSize);
            for (int j = 0; j < partitionSize; j++) {
                partitions.add(buffer.readShort());
            }

            JoyQueueCode code = JoyQueueCode.valueOf(buffer.readInt());
            topicPartitions.put(topic, new FetchAssignedPartitionAckData(partitions, code));
        }

        fetchAssignedPartitionResponse.setTopicPartitions(topicPartitions);
        return fetchAssignedPartitionResponse;
    }

    @Override
    public void encode(FetchAssignedPartitionResponse payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getTopicPartitions().size());
        for (Map.Entry<String, FetchAssignedPartitionAckData> entry : payload.getTopicPartitions().entrySet()) {
            Serializer.write(entry.getKey(), buffer, Serializer.SHORT_SIZE);
            FetchAssignedPartitionAckData data = entry.getValue();
            buffer.writeShort(data.getPartitions().size());
            for (Short partition : data.getPartitions()) {
                buffer.writeShort(partition);
            }
            buffer.writeInt(data.getCode().getCode());
        }
    }

    @Override
    public int type() {
        return JoyQueueCommandType.FETCH_ASSIGNED_PARTITION_RESPONSE.getCode();
    }
}