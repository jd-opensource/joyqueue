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

import io.netty.buffer.ByteBuf;
import org.joyqueue.broker.kafka.KafkaCommandType;
import org.joyqueue.broker.kafka.command.ApiVersionsRequest;
import org.joyqueue.broker.kafka.command.ApiVersionsResponse;
import org.joyqueue.broker.kafka.model.ApiVersion;
import org.joyqueue.broker.kafka.network.KafkaHeader;
import org.joyqueue.broker.kafka.network.KafkaPayloadCodec;
import org.joyqueue.broker.kafka.util.KafkaBufferUtils;
import org.joyqueue.network.transport.command.Type;

/**
 * ApiVersionsCodec
 *
 * author: gaohaoxiang
 * date: 2018/11/5
 */
public class ApiVersionsCodec implements KafkaPayloadCodec<ApiVersionsResponse>, Type {

    @Override
    public Object decode(KafkaHeader header, ByteBuf buffer) throws Exception {
        ApiVersionsRequest apiVersionsRequest = new ApiVersionsRequest();

        // 兼容未知新版本客户端
        if (header.getVersion() > KafkaCommandType.API_VERSIONS.getMaxVersion()) {
            buffer.skipBytes(buffer.readableBytes());
        } else {
            if (header.getVersion() >= 3) {
                // 忽略rawTaggedField
                KafkaBufferUtils.readRawTaggedFields(buffer);
                apiVersionsRequest.setClientSoftwareName(KafkaBufferUtils.readCompactString(buffer));
                apiVersionsRequest.setClientSoftwareVersion(KafkaBufferUtils.readCompactString(buffer));
                KafkaBufferUtils.readRawTaggedFields(buffer);
            }

            // C客户端特殊处理
            if (header.getVersion() < 3) {
                if (buffer.isReadable(4)) {
                    buffer.skipBytes(4);
                }
            }
        }
        return apiVersionsRequest;
    }

    @Override
    public void encode(ApiVersionsResponse payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getErrorCode());
        buffer.writeInt(payload.getApis().size());
        for (ApiVersion api : payload.getApis()) {
            buffer.writeShort(api.getCode());
            buffer.writeShort(api.getMinVersion());
            buffer.writeShort(api.getMaxVersion());
        }
        buffer.writeInt(payload.getThrottleTimeMs());
    }

    @Override
    public int type() {
        return KafkaCommandType.API_VERSIONS.getCode();
    }
}