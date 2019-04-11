package com.jd.journalq.broker.kafka.network.codec;

import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.command.InitProducerIdRequest;
import com.jd.journalq.broker.kafka.command.InitProducerIdResponse;
import com.jd.journalq.broker.kafka.network.KafkaHeader;
import com.jd.journalq.broker.kafka.network.KafkaPayloadCodec;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * InitProducerIdCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/4
 */
public class InitProducerIdCodec implements KafkaPayloadCodec<InitProducerIdResponse>, Type {

    @Override
    public InitProducerIdRequest decode(KafkaHeader header, ByteBuf buffer) throws Exception {
        String transactionId = Serializer.readString(buffer, Serializer.SHORT_SIZE);
        int transactionTimeout = buffer.readInt();

        InitProducerIdRequest initProducerIdRequest = new InitProducerIdRequest();
        initProducerIdRequest.setTransactionId(transactionId);
        initProducerIdRequest.setTransactionTimeout(transactionTimeout);
        return initProducerIdRequest;
    }

    @Override
    public void encode(InitProducerIdResponse payload, ByteBuf buffer) throws Exception {
        buffer.writeInt(payload.getThrottleTimeMs());
        buffer.writeShort(payload.getCode());
        buffer.writeLong(payload.getProducerId());
        buffer.writeShort(payload.getProducerEpoch());
    }

    @Override
    public int type() {
        return KafkaCommandType.INIT_PRODUCER_ID.getCode();
    }
}