package org.joyqueue.broker.protocol.network.codec;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import io.netty.buffer.ByteBuf;
import org.joyqueue.broker.protocol.command.FetchPartitionMessageRequest;
import org.joyqueue.network.command.FetchPartitionMessageData;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.codec.JoyQueueHeader;

/**
 * FetchPartitionMessageRequestCodec
 * author: gaohaoxiang
 * date: 2020/4/7
 */
public class FetchPartitionMessageRequestCodec extends org.joyqueue.network.codec.FetchPartitionMessageRequestCodec
        implements JoyQueuePayloadCodec<org.joyqueue.network.command.FetchPartitionMessageRequest > {

    @Override
    public FetchPartitionMessageRequest decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        Table<String, Short, FetchPartitionMessageData> partitions = HashBasedTable.create();
        int topicSize = buffer.readShort();
        for (int i = 0; i < topicSize; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            int partitionSize = buffer.readShort();
            for (int j = 0; j < partitionSize; j++) {
                short partition = buffer.readShort();
                int count = buffer.readInt();
                long index = buffer.readLong();

                partitions.put(topic, partition, new FetchPartitionMessageData(count, index));
            }
        }

        FetchPartitionMessageRequest fetchPartitionMessageRequest = new FetchPartitionMessageRequest();
        fetchPartitionMessageRequest.setPartitions(partitions);
        fetchPartitionMessageRequest.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        return fetchPartitionMessageRequest;
    }
}