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
import com.google.common.collect.Table;
import com.jd.journalq.network.command.FetchPartitionMessage;
import com.jd.journalq.network.command.FetchPartitionMessageData;
import com.jd.journalq.network.command.JournalqCommandType;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.Map;

/**
 * FetchPartitionMessageCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/13
 */
public class FetchPartitionMessageCodec implements PayloadCodec<JMQHeader, FetchPartitionMessage>, Type {

    @Override
    public FetchPartitionMessage decode(JMQHeader header, ByteBuf buffer) throws Exception {
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

        FetchPartitionMessage fetchPartitionMessage = new FetchPartitionMessage();
        fetchPartitionMessage.setPartitions(partitions);
        fetchPartitionMessage.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        return fetchPartitionMessage;
    }

    @Override
    public void encode(FetchPartitionMessage payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getPartitions().rowMap().size());
        for (Map.Entry<String, Map<Short, FetchPartitionMessageData>> topicEntry : payload.getPartitions().rowMap().entrySet()) {
            Serializer.write(topicEntry.getKey(), buffer, Serializer.SHORT_SIZE);
            buffer.writeShort(topicEntry.getValue().size());
            for (Map.Entry<Short, FetchPartitionMessageData> partitionEntry : topicEntry.getValue().entrySet()) {
                FetchPartitionMessageData fetchPartitionMessageData = partitionEntry.getValue();
                buffer.writeShort(partitionEntry.getKey());
                buffer.writeInt(fetchPartitionMessageData.getCount());
                buffer.writeLong(fetchPartitionMessageData.getIndex());
            }
        }
        Serializer.write(payload.getApp(), buffer, Serializer.SHORT_SIZE);
    }

    @Override
    public int type() {
        return JournalqCommandType.FETCH_PARTITION_MESSAGE.getCode();
    }
}