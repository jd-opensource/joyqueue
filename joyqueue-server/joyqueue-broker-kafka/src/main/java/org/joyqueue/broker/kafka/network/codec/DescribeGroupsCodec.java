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
package org.joyqueue.broker.kafka.network.codec;

import com.google.common.collect.Lists;
import org.joyqueue.broker.kafka.KafkaCommandType;
import org.joyqueue.broker.kafka.command.DescribeGroupsRequest;
import org.joyqueue.broker.kafka.command.DescribeGroupsResponse;
import org.joyqueue.broker.kafka.coordinator.group.domain.GroupMemberMetadata;
import org.joyqueue.broker.kafka.coordinator.group.domain.GroupDescribe;
import org.joyqueue.broker.kafka.message.serializer.KafkaSyncGroupAssignmentSerializer;
import org.joyqueue.broker.kafka.network.KafkaHeader;
import org.joyqueue.broker.kafka.network.KafkaPayloadCodec;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.network.transport.exception.TransportException;
import io.netty.buffer.ByteBuf;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * DescribeGroupsCodec
 *
 * author: gaohaoxiang
 * date: 2018/11/5
 */
public class DescribeGroupsCodec implements KafkaPayloadCodec<DescribeGroupsResponse>, Type {
    @Override
    public Object decode(KafkaHeader header, ByteBuf buffer) throws Exception {
        DescribeGroupsRequest request = new DescribeGroupsRequest();
        List<String> groupIds = Lists.newLinkedList();
        for (int i = 0, count = buffer.readInt(); i < count; i++) {
            groupIds.add(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        }
        request.setGroupIds(groupIds);
        return request;
    }

    @Override
    public void encode(DescribeGroupsResponse payload, ByteBuf buffer) throws Exception {
        DescribeGroupsResponse response = payload;
        short version = response.getVersion();
        if (version == 1) {
            buffer.writeInt(response.getThrottleTimeMs());
        }

        if (CollectionUtils.isEmpty(response.getGroups())) {
            buffer.writeInt(0);
        } else {
            buffer.writeInt(response.getGroups().size());
            for (GroupDescribe group : response.getGroups()) {
                buffer.writeShort(group.getErrCode());
                try {
                    Serializer.write(group.getGroupId(), buffer, Serializer.SHORT_SIZE);
                    Serializer.write(group.getState(), buffer, Serializer.SHORT_SIZE);
                    Serializer.write(group.getProtocolType(), buffer, Serializer.SHORT_SIZE);
                    Serializer.write(group.getProtocol(), buffer, Serializer.SHORT_SIZE);
                } catch (Exception e) {
                    throw new TransportException.CodecException(e);
                }
                if (CollectionUtils.isEmpty(group.getMembers())) {
                    buffer.writeInt(0);
                } else {
                    buffer.writeInt(group.getMembers().size());
                    for (GroupMemberMetadata member : group.getMembers()) {
                        try {
                            Serializer.write(member.getId(), buffer, Serializer.SHORT_SIZE);
                            Serializer.write(member.getClientId(), buffer, Serializer.SHORT_SIZE);
                            Serializer.write(member.getConnectionHost(), buffer, Serializer.SHORT_SIZE);
                        } catch (Exception e) {
                            throw new TransportException.CodecException(e);
                        }
                        byte[] metadata = member.metadata(group.getProtocol());
                        buffer.writeInt(metadata.length);
                        buffer.writeBytes(metadata);
                        KafkaSyncGroupAssignmentSerializer.writeAssignment(buffer, member.getAssignment());
                    }
                }
            }
        }
    }

    @Override
    public int type() {
        return KafkaCommandType.DESCRIBE_GROUP.getCode();
    }
}