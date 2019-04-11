/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jd.journalq.network.codec;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.message.BrokerMessage;
import com.jd.journalq.network.command.FetchPartitionMessageAck;
import com.jd.journalq.network.command.FetchPartitionMessageAckData;
import com.jd.journalq.network.command.JMQCommandType;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

/**
 * FetchPartitionMessageAckCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/13
 */
public class FetchPartitionMessageAckCodec implements PayloadCodec<JMQHeader, FetchPartitionMessageAck>, Type {

    @Override
    public FetchPartitionMessageAck decode(JMQHeader header, ByteBuf buffer) throws Exception {
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
                JMQCode code = JMQCode.valueOf(buffer.readInt());
                FetchPartitionMessageAckData fetchPartitionMessageAckData = new FetchPartitionMessageAckData(messages, code);
                data.put(topic, partition, fetchPartitionMessageAckData);
            }
        }

        FetchPartitionMessageAck fetchPartitionMessageAck = new FetchPartitionMessageAck();
        fetchPartitionMessageAck.setData(data);
        return fetchPartitionMessageAck;
    }

    @Override
    public void encode(FetchPartitionMessageAck payload, ByteBuf buffer) throws Exception {
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
        return JMQCommandType.FETCH_PARTITION_MESSAGE_ACK.getCode();
    }
}