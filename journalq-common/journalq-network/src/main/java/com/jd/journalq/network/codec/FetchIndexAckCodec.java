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
import com.jd.journalq.exception.JournalqCode;
import com.jd.journalq.network.command.FetchIndexAck;
import com.jd.journalq.network.command.FetchIndexAckData;
import com.jd.journalq.network.command.JournalqCommandType;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.Map;

/**
 * FetchIndexAckCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/13
 */
public class FetchIndexAckCodec implements PayloadCodec<JMQHeader, FetchIndexAck>, Type {

    @Override
    public FetchIndexAck decode(JMQHeader header, ByteBuf buffer) throws Exception {
        Table<String, Short, FetchIndexAckData> result = HashBasedTable.create();
        short topicSize = buffer.readShort();
        for (int i = 0; i < topicSize; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            short partitionSize = buffer.readShort();
            for (int j = 0; j < partitionSize; j++) {
                short partition = buffer.readShort();
                long index = buffer.readLong();
                JournalqCode code = JournalqCode.valueOf(buffer.readInt());
                FetchIndexAckData fetchIndexAckData = new FetchIndexAckData(index, code);
                result.put(topic, partition, fetchIndexAckData);
            }
        }

        FetchIndexAck fetchIndexAck = new FetchIndexAck();
        fetchIndexAck.setData(result);
        return fetchIndexAck;
    }

    @Override
    public void encode(FetchIndexAck payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getData().rowMap().size());
        for (Map.Entry<String, Map<Short, FetchIndexAckData>> topicEntry : payload.getData().rowMap().entrySet()) {
            Serializer.write(topicEntry.getKey(), buffer, Serializer.SHORT_SIZE);
            buffer.writeShort(topicEntry.getValue().size());
            for (Map.Entry<Short, FetchIndexAckData> partitionEntry : topicEntry.getValue().entrySet()) {
                FetchIndexAckData fetchIndexAckData = partitionEntry.getValue();
                buffer.writeShort(partitionEntry.getKey());
                buffer.writeLong(fetchIndexAckData.getIndex());
                buffer.writeInt(fetchIndexAckData.getCode().getCode());
            }
        }
    }

    @Override
    public int type() {
        return JournalqCommandType.FETCH_INDEX_ACK.getCode();
    }
}