package com.jd.journalq.broker.kafka.network.codec;

import com.jd.journalq.broker.kafka.coordinator.CoordinatorType;
import com.jd.journalq.broker.kafka.network.KafkaPayloadCodec;
import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.command.FindCoordinatorRequest;
import com.jd.journalq.broker.kafka.command.FindCoordinatorResponse;
import com.jd.journalq.broker.kafka.model.KafkaBroker;
import com.jd.journalq.broker.kafka.network.KafkaHeader;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.StringUtils;

/**
 * FindCoordinatorCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
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