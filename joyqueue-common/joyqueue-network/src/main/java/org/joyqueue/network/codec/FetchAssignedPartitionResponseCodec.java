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
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.network.command.FetchAssignedPartitionAckData;
import org.joyqueue.network.command.FetchAssignedPartitionResponse;
import org.joyqueue.network.command.JoyQueueCommandType;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.codec.JoyQueueHeader;
import org.joyqueue.network.transport.codec.PayloadCodec;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.Map;

/**
 * FetchAssignedPartitionResponseCodec
 *
 * author: gaohaoxiang
 * date: 2018/12/4
 */
public class FetchAssignedPartitionResponseCodec implements PayloadCodec<JoyQueueHeader, FetchAssignedPartitionResponse>, Type {

    @Override
    public FetchAssignedPartitionResponse decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        FetchAssignedPartitionResponse fetchAssignedPartitionResponse = new FetchAssignedPartitionResponse();
        Map<String, FetchAssignedPartitionAckData> topicPartitions = Maps.newHashMap();

        short topicSize = buffer.readShort();
        for (int i = 0; i < topicSize; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);

            int partitionSize = buffer.readShort();
            List<Short> partitions = Lists.newArrayListWithCapacity(partitionSize);
            for (int j = 0; j < partitionSize; j++) {
                partitions.add(buffer.readShort());
            }

            JoyQueueCode code = JoyQueueCode.valueOf(buffer.readInt());
            topicPartitions.put(topic, new FetchAssignedPartitionAckData(partitions, code));
        }

        fetchAssignedPartitionResponse.setTopicPartitions(topicPartitions);
        return fetchAssignedPartitionResponse;
    }

    @Override
    public void encode(FetchAssignedPartitionResponse payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getTopicPartitions().size());
        for (Map.Entry<String, FetchAssignedPartitionAckData> entry : payload.getTopicPartitions().entrySet()) {
            Serializer.write(entry.getKey(), buffer, Serializer.SHORT_SIZE);
            FetchAssignedPartitionAckData data = entry.getValue();
            buffer.writeShort(data.getPartitions().size());
            for (Short partition : data.getPartitions()) {
                buffer.writeShort(partition);
            }
            buffer.writeInt(data.getCode().getCode());
        }
    }

    @Override
    public int type() {
        return JoyQueueCommandType.FETCH_ASSIGNED_PARTITION_RESPONSE.getCode();
    }
}