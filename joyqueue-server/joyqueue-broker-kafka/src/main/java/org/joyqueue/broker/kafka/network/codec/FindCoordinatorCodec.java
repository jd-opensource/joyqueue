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

import org.joyqueue.broker.kafka.coordinator.CoordinatorType;
import org.joyqueue.broker.kafka.network.KafkaPayloadCodec;
import org.joyqueue.broker.kafka.KafkaCommandType;
import org.joyqueue.broker.kafka.command.FindCoordinatorRequest;
import org.joyqueue.broker.kafka.command.FindCoordinatorResponse;
import org.joyqueue.broker.kafka.model.KafkaBroker;
import org.joyqueue.broker.kafka.network.KafkaHeader;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.StringUtils;

/**
 * FindCoordinatorRequestCodec
 *
 * author: gaohaoxiang
 * date: 2018/11/5
 */
public class FindCoordinatorCodec implements KafkaPayloadCodec<FindCoordinatorResponse>, Type {

    @Override
    public FindCoordinatorRequest decode(KafkaHeader header, ByteBuf buffer) throws Exception {
        FindCoordinatorRequest request = new FindCoordinatorRequest();
        request.setCoordinatorKey(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        if (header.getVersion() >= 1) {
            request.setCoordinatorType(CoordinatorType.valueOf(buffer.readByte()));
        }
        return request;
    }

    @Override
    public void encode(FindCoordinatorResponse payload, ByteBuf buffer) throws Exception {
        short version = payload.getVersion();
        if (version >= 1) {
            // throttle_time_ms
            buffer.writeInt(payload.getThrottleTimeMs());
        }

        // 错误码
        buffer.writeShort(payload.getErrorCode());
        if (version >= 1) {
            Serializer.write(StringUtils.EMPTY, buffer, Serializer.SHORT_SIZE);
        }

        KafkaBroker broker = payload.getBroker();
        buffer.writeInt(broker.getId());
        Serializer.write(broker.getHost(), buffer, Serializer.SHORT_SIZE);
        buffer.writeInt(broker.getPort());
    }

    @Override
    public int type() {
        return KafkaCommandType.FIND_COORDINATOR.getCode();
    }
}