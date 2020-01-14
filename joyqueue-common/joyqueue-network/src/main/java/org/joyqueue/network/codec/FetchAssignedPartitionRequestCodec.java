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
import org.joyqueue.network.command.FetchAssignedPartitionData;
import org.joyqueue.network.command.FetchAssignedPartitionRequest;
import org.joyqueue.network.command.JoyQueueCommandType;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.codec.JoyQueueHeader;
import org.joyqueue.network.transport.codec.PayloadCodec;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.List;

/**
 * FetchAssignedPartitionRequestCodec
 *
 * author: gaohaoxiang
 * date: 2018/12/4
 */
public class FetchAssignedPartitionRequestCodec implements PayloadCodec<JoyQueueHeader, FetchAssignedPartitionRequest>, Type {

    @Override
    public FetchAssignedPartitionRequest decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        short dataSize = buffer.readShort();
        List<FetchAssignedPartitionData> data = Lists.newArrayListWithCapacity(dataSize);
        for (int i = 0; i < dataSize; i++) {
            FetchAssignedPartitionData fetchAssignedPartitionData = new FetchAssignedPartitionData();
            fetchAssignedPartitionData.setTopic(Serializer.readString(buffer, Serializer.SHORT_SIZE));
            fetchAssignedPartitionData.setSessionTimeout(buffer.readInt());
            fetchAssignedPartitionData.setNearby(buffer.readBoolean());
            data.add(fetchAssignedPartitionData);
        }

        FetchAssignedPartitionRequest fetchAssignedPartitionRequest = new FetchAssignedPartitionRequest();
        fetchAssignedPartitionRequest.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        fetchAssignedPartitionRequest.setData(data);
        return fetchAssignedPartitionRequest;
    }

    @Override
    public void encode(FetchAssignedPartitionRequest payload, ByteBuf buffer) throws Exception {
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
        return JoyQueueCommandType.FETCH_ASSIGNED_PARTITION_REQUEST.getCode();
    }
}