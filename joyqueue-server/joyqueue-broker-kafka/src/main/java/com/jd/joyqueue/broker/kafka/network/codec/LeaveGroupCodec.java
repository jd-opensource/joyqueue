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
package com.jd.joyqueue.broker.kafka.network.codec;

import com.jd.joyqueue.broker.kafka.network.KafkaPayloadCodec;
import com.jd.joyqueue.broker.kafka.KafkaCommandType;
import com.jd.joyqueue.broker.kafka.command.LeaveGroupRequest;
import com.jd.joyqueue.broker.kafka.command.LeaveGroupResponse;
import com.jd.joyqueue.broker.kafka.network.KafkaHeader;
import com.jd.joyqueue.network.serializer.Serializer;
import com.jd.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * LeaveGroupCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/5
 */
public class LeaveGroupCodec implements KafkaPayloadCodec<LeaveGroupResponse>, Type {

    @Override
    public Object decode(KafkaHeader header, ByteBuf buffer) throws Exception {
        LeaveGroupRequest request = new LeaveGroupRequest();
        request.setGroupId(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        request.setMemberId(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        return request;
    }

    @Override
    public void encode(LeaveGroupResponse payload, ByteBuf buffer) throws Exception {
        if (payload.getVersion() >= 1) {
            // throttle_time_ms
            buffer.writeInt(payload.getThrottleTimeMs());
        }

        // 错误码
        buffer.writeShort(payload.getErrorCode());
    }

    @Override
    public int type() {
        return KafkaCommandType.LEAVE_GROUP.getCode();
    }
}