package com.jd.journalq.broker.kafka.network.codec;

import com.jd.journalq.broker.kafka.network.KafkaPayloadCodec;
import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.command.HeartbeatRequest;
import com.jd.journalq.broker.kafka.command.HeartbeatResponse;
import com.jd.journalq.broker.kafka.network.KafkaHeader;
import com.jd.journalq.common.network.serializer.Serializer;
import com.jd.journalq.common.network.transport.command.Type;
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