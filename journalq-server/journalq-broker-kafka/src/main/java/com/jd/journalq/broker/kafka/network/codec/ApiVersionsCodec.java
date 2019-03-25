package com.jd.journalq.broker.kafka.network.codec;

import com.jd.journalq.broker.kafka.network.KafkaHeader;
import com.jd.journalq.broker.kafka.network.KafkaPayloadCodec;
import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.command.ApiVersionsRequest;
import com.jd.journalq.broker.kafka.command.ApiVersionsResponse;
import com.jd.journalq.broker.kafka.model.ApiVersion;
import com.jd.journalq.network.transport.command.Type;
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