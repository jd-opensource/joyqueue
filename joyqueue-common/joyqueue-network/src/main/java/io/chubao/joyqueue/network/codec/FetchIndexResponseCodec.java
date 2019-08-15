/**
 * Copyright 2018 The JoyQueue Authors.
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
package io.chubao.joyqueue.network.codec;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.network.command.FetchIndexAckData;
import io.chubao.joyqueue.network.command.FetchIndexResponse;
import io.chubao.joyqueue.network.command.JoyQueueCommandType;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.codec.PayloadCodec;
import io.chubao.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.Map;

/**
 * FetchIndexResponseCodec
 *
 * author: gaohaoxiang
 * date: 2018/12/13
 */
public class FetchIndexResponseCodec implements PayloadCodec<JoyQueueHeader, FetchIndexResponse>, Type {

    @Override
    public FetchIndexResponse decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        Table<String, Short, FetchIndexAckData> result = HashBasedTable.create();
        short topicSize = buffer.readShort();
        for (int i = 0; i < topicSize; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            short partitionSize = buffer.readShort();
            for (int j = 0; j < partitionSize; j++) {
                short partition = buffer.readShort();
                long index = buffer.readLong();
                JoyQueueCode code = JoyQueueCode.valueOf(buffer.readInt());
                FetchIndexAckData fetchIndexAckData = new FetchIndexAckData(index, code);
                result.put(topic, partition, fetchIndexAckData);
            }
        }

        FetchIndexResponse fetchIndexResponse = new FetchIndexResponse();
        fetchIndexResponse.setData(result);
        return fetchIndexResponse;
    }

    @Override
    public void encode(FetchIndexResponse payload, ByteBuf buffer) throws Exception {
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
        return JoyQueueCommandType.FETCH_INDEX_RESPONSE.getCode();
    }
}