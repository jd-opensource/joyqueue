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
package org.joyqueue.broker.index.network.codec;

import org.joyqueue.broker.index.command.ConsumeIndexQueryRequest;
import org.joyqueue.network.command.CommandType;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.codec.JoyQueueHeader;
import org.joyqueue.network.transport.codec.PayloadDecoder;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;
import org.apache.commons.collections.map.HashedMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by zhuduohui on 2018/9/7.
 */
public class IndexQueryRequestDecoder implements PayloadDecoder<JoyQueueHeader>, Type {

    @Override
    public Object decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        Map<String, List<Integer>> indexTopicPartitions = new HashedMap();
        String groupId = Serializer.readString(buffer, Serializer.SHORT_SIZE);
        int topics = buffer.readInt();
        for (int i = 0; i < topics; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            List<Integer> partitions = new ArrayList<>();
            int partitionNum = buffer.readInt();
            for (int j = 0; j < partitionNum; j++) {
                partitions.add(buffer.readInt());
            }
            indexTopicPartitions.put(topic, partitions);
        }
        return new ConsumeIndexQueryRequest(groupId, indexTopicPartitions);
    }

    @Override
    public int type() {
        return CommandType.CONSUME_INDEX_QUERY_REQUEST;
    }
}
