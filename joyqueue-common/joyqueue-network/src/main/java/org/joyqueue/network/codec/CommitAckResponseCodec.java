/**
 * Copyright 2019 The JoyQueue Authors.
 *
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
package org.joyqueue.network.codec;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.network.command.CommitAckResponse;
import org.joyqueue.network.command.JoyQueueCommandType;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.codec.JoyQueueHeader;
import org.joyqueue.network.transport.codec.PayloadCodec;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.Map;

/**
 * CommitAckResponseCodec
 *
 * author: gaohaoxiang
 * date: 2018/12/12
 */
public class CommitAckResponseCodec implements PayloadCodec<JoyQueueHeader, CommitAckResponse>, Type {

    @Override
    public CommitAckResponse decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        short size = buffer.readShort();
        Table<String, Short, JoyQueueCode> result = HashBasedTable.create();

        for (int i = 0; i < size; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            short partitionSize = buffer.readShort();
            for (int j = 0; j < partitionSize; j++) {
                short partition = buffer.readShort();
                result.put(topic, partition, JoyQueueCode.valueOf(buffer.readInt()));
            }
        }

        CommitAckResponse commitAckResponse = new CommitAckResponse();
        commitAckResponse.setResult(result);
        return commitAckResponse;
    }

    @Override
    public void encode(CommitAckResponse payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getResult().size());
        for (Map.Entry<String, Map<Short, JoyQueueCode>> entry : payload.getResult().rowMap().entrySet()) {
            Serializer.write(entry.getKey(), buffer, Serializer.SHORT_SIZE);
            buffer.writeShort(entry.getValue().size());
            for (Map.Entry<Short, JoyQueueCode> partitionEntry : entry.getValue().entrySet()) {
                buffer.writeShort(partitionEntry.getKey());
                buffer.writeInt(partitionEntry.getValue().getCode());
            }
        }
    }

    @Override
    public int type() {
        return JoyQueueCommandType.COMMIT_ACK_RESPONSE.getCode();
    }
}