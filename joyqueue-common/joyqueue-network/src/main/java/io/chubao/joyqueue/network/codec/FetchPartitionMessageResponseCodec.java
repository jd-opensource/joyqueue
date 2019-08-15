package io.chubao.joyqueue.network.codec;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.message.BrokerMessage;
import io.chubao.joyqueue.network.command.FetchPartitionMessageResponse;
import io.chubao.joyqueue.network.command.FetchPartitionMessageAckData;
import io.chubao.joyqueue.network.command.JoyQueueCommandType;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.codec.PayloadCodec;
import io.chubao.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

/**
 * FetchPartitionMessageResponseCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/13
 */
public class FetchPartitionMessageResponseCodec implements PayloadCodec<JoyQueueHeader, FetchPartitionMessageResponse>, Type {

    @Override
    public FetchPartitionMessageResponse decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        Table<String, Short, FetchPartitionMessageAckData> data = HashBasedTable.create();
        short topicSize = buffer.readShort();
        for (int i = 0; i < topicSize; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            int partitionSize = buffer.readShort();
            for (int j = 0; j < partitionSize; j++) {
                short partition = buffer.readShort();
                short messageSize = buffer.readShort();
                List<BrokerMessage> messages = Lists.newArrayListWithCapacity(messageSize);
                for (int k = 0; k < messageSize; k++) {
                    messages.add(Serializer.readBrokerMessage(buffer));
                }
                JoyQueueCode code = JoyQueueCode.valueOf(buffer.readInt());
                FetchPartitionMessageAckData fetchPartitionMessageAckData = new FetchPartitionMessageAckData(messages, code);
                data.put(topic, partition, fetchPartitionMessageAckData);
            }
        }

        FetchPartitionMessageResponse fetchPartitionMessageResponse = new FetchPartitionMessageResponse();
        fetchPartitionMessageResponse.setData(data);
        return fetchPartitionMessageResponse;
    }

    @Override
    public void encode(FetchPartitionMessageResponse payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getData().rowMap().size());
        for (Map.Entry<String, Map<Short, FetchPartitionMessageAckData>> topicEntry : payload.getData().rowMap().entrySet()) {
            Serializer.write(topicEntry.getKey(), buffer, Serializer.SHORT_SIZE);
            buffer.writeShort(topicEntry.getValue().size());
            for (Map.Entry<Short, FetchPartitionMessageAckData> partitionEntry : topicEntry.getValue().entrySet()) {
                FetchPartitionMessageAckData fetchPartitionMessageAckData = partitionEntry.getValue();
                buffer.writeShort(partitionEntry.getKey());
                buffer.writeShort(fetchPartitionMessageAckData.getBuffers().size());
                for (ByteBuffer rByteBuffer : fetchPartitionMessageAckData.getBuffers()) {
                    buffer.writeBytes(rByteBuffer);
                }
                buffer.writeInt(fetchPartitionMessageAckData.getCode().getCode());
            }
        }
    }

    @Override
    public int type() {
        return JoyQueueCommandType.FETCH_PARTITION_MESSAGE_RESPONSE.getCode();
    }
}