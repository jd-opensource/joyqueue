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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joyqueue.network.command.FetchIndexRequest;
import org.joyqueue.network.command.JoyQueueCommandType;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.codec.JoyQueueHeader;
import org.joyqueue.network.transport.codec.PayloadCodec;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.Map;

/**
 * FetchIndexRequestCodec
 *
 * author: gaohaoxiang
 * date: 2018/12/13
 */
public class FetchIndexRequestCodec implements PayloadCodec<JoyQueueHeader, FetchIndexRequest>, Type {

    @Override
    public FetchIndexRequest decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        Map<String, List<Short>> result = Maps.newHashMap();
        short topicSize = buffer.readShort();
        for (int i = 0; i < topicSize; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            short partitionSize = buffer.readShort();
            List<Short> partitions = Lists.newLinkedList();
            for (int j = 0; j < partitionSize; j++) {
                partitions.add(buffer.readShort());
            }
            result.put(topic, partitions);
        }

        FetchIndexRequest fetchIndexRequest = new FetchIndexRequest();
        fetchIndexRequest.setPartitions(result);
        fetchIndexRequest.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        return fetchIndexRequest;
    }

    @Override
    public void encode(FetchIndexRequest payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getPartitions().size());
        for (Map.Entry<String, List<Short>> entry : payload.getPartitions().entrySet()) {
            Serializer.write(entry.getKey(), buffer, Serializer.SHORT_SIZE);
            buffer.writeShort(entry.getValue().size());
            for (Short partition : entry.getValue()) {
                buffer.writeShort(partition);
            }
        }
        Serializer.write(payload.getApp(), buffer, Serializer.SHORT_SIZE);
    }

    @Override
    public int type() {
        return JoyQueueCommandType.FETCH_INDEX_REQUEST.getCode();
    }
}