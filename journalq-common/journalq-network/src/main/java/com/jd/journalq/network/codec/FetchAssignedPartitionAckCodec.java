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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jd.journalq.exception.JournalqCode;
import com.jd.journalq.network.command.FetchAssignedPartitionAck;
import com.jd.journalq.network.command.FetchAssignedPartitionAckData;
import com.jd.journalq.network.command.JournalqCommandType;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.Map;

/**
 * FetchAssignedPartitionAckCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/4
 */
public class FetchAssignedPartitionAckCodec implements PayloadCodec<JMQHeader, FetchAssignedPartitionAck>, Type {

    @Override
    public FetchAssignedPartitionAck decode(JMQHeader header, ByteBuf buffer) throws Exception {
        FetchAssignedPartitionAck fetchAssignedPartitionAck = new FetchAssignedPartitionAck();
        Map<String, FetchAssignedPartitionAckData> topicPartitions = Maps.newHashMap();

        short topicSize = buffer.readShort();
        for (int i = 0; i < topicSize; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);

            int partitionSize = buffer.readShort();
            List<Short> partitions = Lists.newArrayListWithCapacity(partitionSize);
            for (int j = 0; j < partitionSize; j++) {
                partitions.add(buffer.readShort());
            }

            JournalqCode code = JournalqCode.valueOf(buffer.readInt());
            topicPartitions.put(topic, new FetchAssignedPartitionAckData(partitions, code));
        }

        fetchAssignedPartitionAck.setTopicPartitions(topicPartitions);
        return fetchAssignedPartitionAck;
    }

    @Override
    public void encode(FetchAssignedPartitionAck payload, ByteBuf buffer) throws Exception {
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
        return JournalqCommandType.FETCH_ASSIGNED_PARTITION_ACK.getCode();
    }
}