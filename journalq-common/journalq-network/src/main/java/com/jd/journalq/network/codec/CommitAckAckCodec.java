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
import com.jd.journalq.network.command.CommitAckAck;
import com.jd.journalq.network.command.JournalqCommandType;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.Map;

/**
 * CommitAckAckCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/12
 */
public class CommitAckAckCodec implements PayloadCodec<JMQHeader, CommitAckAck>, Type {

    @Override
    public CommitAckAck decode(JMQHeader header, ByteBuf buffer) throws Exception {
        short size = buffer.readShort();
        Table<String, Short, JournalqCode> result = HashBasedTable.create();

        for (int i = 0; i < size; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            short partitionSize = buffer.readShort();
            for (int j = 0; j < partitionSize; j++) {
                short partition = buffer.readShort();
                result.put(topic, partition, JournalqCode.valueOf(buffer.readInt()));
            }
        }

        CommitAckAck commitAckAck = new CommitAckAck();
        commitAckAck.setResult(result);
        return commitAckAck;
    }

    @Override
    public void encode(CommitAckAck payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getResult().size());
        for (Map.Entry<String, Map<Short, JournalqCode>> entry : payload.getResult().rowMap().entrySet()) {
            Serializer.write(entry.getKey(), buffer, Serializer.SHORT_SIZE);
            buffer.writeShort(entry.getValue().size());
            for (Map.Entry<Short, JournalqCode> partitionEntry : entry.getValue().entrySet()) {
                buffer.writeShort(partitionEntry.getKey());
                buffer.writeInt(partitionEntry.getValue().getCode());
            }
        }
    }

    @Override
    public int type() {
        return JournalqCommandType.COMMIT_ACK_ACK.getCode();
    }
}