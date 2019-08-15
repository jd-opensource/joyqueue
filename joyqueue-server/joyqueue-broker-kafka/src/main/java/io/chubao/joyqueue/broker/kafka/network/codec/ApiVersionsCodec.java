package io.chubao.joyqueue.broker.kafka.network.codec;

import io.chubao.joyqueue.broker.kafka.network.KafkaHeader;
import io.chubao.joyqueue.broker.kafka.network.KafkaPayloadCodec;
import io.chubao.joyqueue.broker.kafka.KafkaCommandType;
import io.chubao.joyqueue.broker.kafka.command.ApiVersionsRequest;
import io.chubao.joyqueue.broker.kafka.command.ApiVersionsResponse;
import io.chubao.joyqueue.broker.kafka.model.ApiVersion;
import io.chubao.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * ApiVersionsCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/5
 */
public class ApiVersionsCodec implements KafkaPayloadCodec<ApiVersionsResponse>, Type {

    @Override
    public Object decode(KafkaHeader header, ByteBuf buffer) throws Exception {
        if (buffer.isReadable(4)) {
            buffer.skipBytes(4);
        }
        return new ApiVersionsRequest();
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