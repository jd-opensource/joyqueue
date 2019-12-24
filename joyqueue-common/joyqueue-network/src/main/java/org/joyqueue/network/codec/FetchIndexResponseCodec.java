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
import org.joyqueue.network.command.FetchIndexData;
import org.joyqueue.network.command.FetchIndexResponse;
import org.joyqueue.network.command.JoyQueueCommandType;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.codec.JoyQueueHeader;
import org.joyqueue.network.transport.codec.PayloadCodec;
import org.joyqueue.network.transport.command.Type;
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
        Table<String, Short, FetchIndexData> result = HashBasedTable.create();
        short topicSize = buffer.readShort();
        for (int i = 0; i < topicSize; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            short partitionSize = buffer.readShort();
            for (int j = 0; j < partitionSize; j++) {
                short partition = buffer.readShort();
                long index = buffer.readLong();
                JoyQueueCode code = JoyQueueCode.valueOf(buffer.readInt());
                long leftIndex = -1;
                long rightIndex = -1;

                if (header.getVersion() >= JoyQueueHeader.VERSION_V3) {
                    leftIndex = buffer.readLong();
                    rightIndex = buffer.readLong();
                }

                FetchIndexData fetchIndexData = new FetchIndexData(index, leftIndex, rightIndex, code);
                result.put(topic, partition, fetchIndexData);
            }
        }

        FetchIndexResponse fetchIndexResponse = new FetchIndexResponse();
        fetchIndexResponse.setData(result);
        return fetchIndexResponse;
    }

    @Override
    public void encode(FetchIndexResponse payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getData().rowMap().size());
        for (Map.Entry<String, Map<Short, FetchIndexData>> topicEntry : payload.getData().rowMap().entrySet()) {
            Serializer.write(topicEntry.getKey(), buffer, Serializer.SHORT_SIZE);
            buffer.writeShort(topicEntry.getValue().size());
            for (Map.Entry<Short, FetchIndexData> partitionEntry : topicEntry.getValue().entrySet()) {
                FetchIndexData fetchIndexData = partitionEntry.getValue();
                buffer.writeShort(partitionEntry.getKey());
                buffer.writeLong(fetchIndexData.getIndex());
                buffer.writeInt(fetchIndexData.getCode().getCode());

                if (payload.getHeader().getVersion() >= JoyQueueHeader.VERSION_V3) {
                    buffer.writeLong(fetchIndexData.getLeftIndex());
                    buffer.writeLong(fetchIndexData.getRightIndex());
                }
            }
        }
    }

    @Override
    public int type() {
        return JoyQueueCommandType.FETCH_INDEX_RESPONSE.getCode();
    }
}