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
package io.chubao.joyqueue.broker.kafka.network.codec;

import io.chubao.joyqueue.broker.kafka.network.KafkaPayloadCodec;
import io.chubao.joyqueue.broker.kafka.KafkaCommandType;
import io.chubao.joyqueue.broker.kafka.command.HeartbeatRequest;
import io.chubao.joyqueue.broker.kafka.command.HeartbeatResponse;
import io.chubao.joyqueue.broker.kafka.network.KafkaHeader;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * HeartbeatCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/5
 */
public class HeartbeatCodec implements KafkaPayloadCodec<HeartbeatResponse>, Type {

    @Override
    public HeartbeatRequest decode(KafkaHeader header, ByteBuf buffer) throws Exception {
        HeartbeatRequest request = new HeartbeatRequest();
        request.setGroupId(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        request.setGroupGenerationId(buffer.readInt());
        request.setMemberId(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        return request;
    }

    @Override
    public void encode(HeartbeatResponse payload, ByteBuf buffer) throws Exception {
        HeartbeatResponse response = payload;
        if (response.getVersion() >= 1) {
            // throttle_time_ms
            buffer.writeInt(payload.getThrottleTimeMs());
        }
        // 错误码
        buffer.writeShort(response.getErrorCode());
    }

    @Override
    public int type() {
        return KafkaCommandType.HEARTBEAT.getCode();
    }
}