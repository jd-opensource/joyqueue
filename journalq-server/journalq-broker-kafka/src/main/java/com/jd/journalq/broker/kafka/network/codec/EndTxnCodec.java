package com.jd.journalq.broker.kafka.network.codec;

import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.command.EndTxnRequest;
import com.jd.journalq.broker.kafka.command.EndTxnResponse;
import com.jd.journalq.broker.kafka.network.KafkaHeader;
import com.jd.journalq.broker.kafka.network.KafkaPayloadCodec;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * EndTxnCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/4
 */
public class EndTxnCodec implements KafkaPayloadCodec<EndTxnResponse>, Type {

    @Override
    public EndTxnRequest decode(KafkaHeader header, ByteBuf buffer) throws Exception {
        String transactionId = Serializer.readString(buffer, Serializer.SHORT_SIZE);
        long producerId = buffer.readLong();
        short producerEpoch = buffer.readShort();
        boolean transactionResult = (buffer.readByte() == 1);

        EndTxnRequest endTxnRequest = new EndTxnRequest();
        endTxnRequest.setTransactionId(transactionId);
        endTxnRequest.setProducerId(producerId);
        endTxnRequest.setProducerEpoch(producerEpoch);
        endTxnRequest.setTransactionResult(transactionResult);
        return endTxnRequest;
    }

    @Override
    public void encode(EndTxnResponse payload, ByteBuf buffer) throws Exception {
        buffer.writeInt(payload.getThrottleTimeMs());
        buffer.writeShort(payload.getCode());
    }

    @Override
    public int type() {
        return KafkaCommandType.END_TXN.getCode();
    }
}