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
package com.jd.joyqueue.network.codec;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.jd.joyqueue.network.command.CommitAckData;
import com.jd.joyqueue.network.command.CommitAckRequest;
import com.jd.joyqueue.network.command.JournalqCommandType;
import com.jd.joyqueue.network.command.RetryType;
import com.jd.joyqueue.network.serializer.Serializer;
import com.jd.joyqueue.network.transport.codec.JournalqHeader;
import com.jd.joyqueue.network.transport.codec.PayloadCodec;
import com.jd.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.Map;

/**
 * CommitAckRequestCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/12
 */
public class CommitAckRequestCodec implements PayloadCodec<JournalqHeader, CommitAckRequest>, Type {

    @Override
    public CommitAckRequest decode(JournalqHeader header, ByteBuf buffer) throws Exception {
        short size = buffer.readShort();
        Table<String, Short, List<CommitAckData>> data = HashBasedTable.create();

        for (int i = 0; i < size; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            short partitionSize = buffer.readShort();
            for (int j = 0; j < partitionSize; j++) {
                short partition = buffer.readShort();
                short dataSize = buffer.readShort();
                List<CommitAckData> dataList = Lists.newArrayListWithCapacity(dataSize);
                for (int k = 0; k < dataSize; k++) {
                    CommitAckData commitAckData = new CommitAckData();
                    commitAckData.setPartition(buffer.readShort());
                    commitAckData.setIndex(buffer.readLong());
                    commitAckData.setRetryType(RetryType.valueOf(buffer.readByte()));
                    dataList.add(commitAckData);
                }
                data.put(topic, partition, dataList);
            }
        }

        CommitAckRequest commitAckRequest = new CommitAckRequest();
        commitAckRequest.setData(data);
        commitAckRequest.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        return commitAckRequest;
    }

    @Override
    public void encode(CommitAckRequest payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getData().rowMap().size());
        for (Map.Entry<String, Map<Short, List<CommitAckData>>> entry : payload.getData().rowMap().entrySet()) {
            Serializer.write(entry.getKey(), buffer, Serializer.SHORT_SIZE);
            buffer.writeShort(entry.getValue().size());
            for (Map.Entry<Short, List<CommitAckData>> partitionEntry : entry.getValue().entrySet()) {
                buffer.writeShort(partitionEntry.getKey());
                buffer.writeShort(partitionEntry.getValue().size());
                for (CommitAckData commitAckData : partitionEntry.getValue()) {
                    buffer.writeShort(commitAckData.getPartition());
                    buffer.writeLong(commitAckData.getIndex());
                    buffer.writeByte(commitAckData.getRetryType().getType());
                }
            }
        }
        Serializer.write(payload.getApp(), buffer, Serializer.SHORT_SIZE);
    }

    @Override
    public int type() {
        return JournalqCommandType.COMMIT_ACK_REQUEST.getCode();
    }
}