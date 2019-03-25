package com.jd.journalq.broker.kafka.network.codec;

import com.jd.journalq.broker.kafka.network.KafkaPayloadCodec;
import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.command.LeaveGroupRequest;
import com.jd.journalq.broker.kafka.command.LeaveGroupResponse;
import com.jd.journalq.broker.kafka.network.KafkaHeader;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.command.Type;
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