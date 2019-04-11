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
package com.jd.journalq.broker.index.network.codec;

import com.jd.journalq.broker.index.command.ConsumeIndexQueryRequest;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadDecoder;
import com.jd.journalq.network.command.CommandType;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.command.Type;

import io.netty.buffer.ByteBuf;
import org.apache.commons.collections.map.HashedMap;

import java.util.*;

/**
 * Created by zhuduohui on 2018/9/7.
 */
public class IndexQueryRequestDecoder implements PayloadDecoder<JMQHeader>, Type {

    @Override
    public Object decode(JMQHeader header, ByteBuf buffer) throws Exception {
        Map<String, Set<Integer>> indexTopicPartitions = new HashedMap();
        String groupId = Serializer.readString(buffer, Serializer.SHORT_SIZE);
        int topics = buffer.readInt();
        for (int i = 0; i < topics; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            Set<Integer> partitions = new HashSet<>();
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
