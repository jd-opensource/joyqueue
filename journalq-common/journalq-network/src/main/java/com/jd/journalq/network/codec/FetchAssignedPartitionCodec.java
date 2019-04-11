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

import com.google.common.collect.Lists;
import com.jd.journalq.network.command.FetchAssignedPartition;
import com.jd.journalq.network.command.FetchAssignedPartitionData;
import com.jd.journalq.network.command.JMQCommandType;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.List;

/**
 * FetchAssignedPartitionCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/4
 */
public class FetchAssignedPartitionCodec implements PayloadCodec<JMQHeader, FetchAssignedPartition>, Type {

    @Override
    public FetchAssignedPartition decode(JMQHeader header, ByteBuf buffer) throws Exception {
        short dataSize = buffer.readShort();
        List<FetchAssignedPartitionData> data = Lists.newArrayListWithCapacity(dataSize);
        for (int i = 0; i < dataSize; i++) {
            FetchAssignedPartitionData fetchAssignedPartitionData = new FetchAssignedPartitionData();
            fetchAssignedPartitionData.setTopic(Serializer.readString(buffer, Serializer.SHORT_SIZE));
            fetchAssignedPartitionData.setSessionTimeout(buffer.readInt());
            fetchAssignedPartitionData.setNearby(buffer.readBoolean());
            data.add(fetchAssignedPartitionData);
        }

        FetchAssignedPartition fetchAssignedPartition = new FetchAssignedPartition();
        fetchAssignedPartition.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        fetchAssignedPartition.setData(data);
        return fetchAssignedPartition;
    }

    @Override
    public void encode(FetchAssignedPartition payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getData().size());
        for (FetchAssignedPartitionData fetchAssignedPartitionData : payload.getData()) {
            Serializer.write(fetchAssignedPartitionData.getTopic(), buffer, Serializer.SHORT_SIZE);
            buffer.writeInt(fetchAssignedPartitionData.getSessionTimeout());
            buffer.writeBoolean(fetchAssignedPartitionData.isNearby());
        }
        Serializer.write(payload.getApp(), buffer, Serializer.SHORT_SIZE);
    }

    @Override
    public int type() {
        return JMQCommandType.FETCH_ASSIGNED_PARTITION.getCode();
    }
}