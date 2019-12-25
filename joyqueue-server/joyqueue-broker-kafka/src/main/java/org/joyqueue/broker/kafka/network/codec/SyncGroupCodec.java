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

import com.google.common.collect.Maps;
import org.joyqueue.broker.kafka.command.SyncGroupAssignment;
import org.joyqueue.broker.kafka.message.serializer.KafkaSyncGroupAssignmentSerializer;
import org.joyqueue.broker.kafka.network.KafkaPayloadCodec;
import org.joyqueue.broker.kafka.KafkaCommandType;
import org.joyqueue.broker.kafka.command.SyncGroupRequest;
import org.joyqueue.broker.kafka.command.SyncGroupResponse;
import org.joyqueue.broker.kafka.network.KafkaHeader;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.Collections;
import java.util.Map;

/**
 * SyncGroupCodec
 *
 * author: gaohaoxiang
 * date: 2018/11/5
 */
public class SyncGroupCodec implements KafkaPayloadCodec<SyncGroupResponse>, Type {

    @Override
    public Object decode(KafkaHeader header, ByteBuf buffer) throws Exception {
        SyncGroupRequest request = new SyncGroupRequest();
        Map<String, SyncGroupAssignment> groupAssignment = Collections.emptyMap();

        request.setGroupId(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        request.setGenerationId(buffer.readInt());
        request.setMemberId(Serializer.readString(buffer, Serializer.SHORT_SIZE));

        int size = buffer.readInt();

        if (size > 0) {
            groupAssignment = Maps.newHashMap();
            for (int i = 0; i < size; i++) {
                String memberId = Serializer.readString(buffer, Serializer.SHORT_SIZE);
                SyncGroupAssignment assignment = KafkaSyncGroupAssignmentSerializer.readAssignment(buffer);
                groupAssignment.put(memberId, assignment);
            }
        }

        request.setGroupAssignment(groupAssignment);
        return request;
    }

    @Override
    public void encode(SyncGroupResponse payload, ByteBuf buffer) throws Exception {
        if (payload.getVersion() >= 1) {
            // throttle_time_ms
            buffer.writeInt(payload.getThrottleTimeMs());
        }

        // 错误码
        buffer.writeShort(payload.getErrorCode());

        SyncGroupAssignment assignment = payload.getAssignment();
        if (assignment != null) {
            KafkaSyncGroupAssignmentSerializer.writeAssignment(buffer, assignment);
        } else {
            buffer.writeInt(0);
        }
    }

    @Override
    public int type() {
        return KafkaCommandType.SYNC_GROUP.getCode();
    }
}