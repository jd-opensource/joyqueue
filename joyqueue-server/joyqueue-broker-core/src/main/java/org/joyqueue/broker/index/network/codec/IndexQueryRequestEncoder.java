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
import org.joyqueue.network.transport.codec.PayloadEncoder;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.Map;

/**
 * Created by zhuduohui on 2018/9/7.
 */
public class IndexQueryRequestEncoder implements PayloadEncoder<ConsumeIndexQueryRequest>, Type {

    @Override
    public void encode(ConsumeIndexQueryRequest payload, ByteBuf buf) throws Exception {
        Map<String, List<Integer>> indexTopicPartitions = payload.getTopicPartitions();
        Serializer.write(payload.getApp(), buf, Serializer.SHORT_SIZE);
        buf.writeInt(indexTopicPartitions.size());
        for (String topic : indexTopicPartitions.keySet()) {
            Serializer.write(topic, buf, Serializer.SHORT_SIZE);
            List<Integer> partitions = indexTopicPartitions.get(topic);
            buf.writeInt(partitions.size());
            for (int partition : partitions) {
                buf.writeInt(partition);
            }
        }
    }

    @Override
    public int type() {
        return CommandType.CONSUME_INDEX_QUERY_REQUEST;
    }
}
