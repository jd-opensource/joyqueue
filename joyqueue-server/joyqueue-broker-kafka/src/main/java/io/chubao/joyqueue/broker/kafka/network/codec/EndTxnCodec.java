package io.chubao.joyqueue.broker.kafka.network.codec;

import io.chubao.joyqueue.broker.kafka.KafkaCommandType;
import io.chubao.joyqueue.broker.kafka.command.EndTxnRequest;
import io.chubao.joyqueue.broker.kafka.command.EndTxnResponse;
import io.chubao.joyqueue.broker.kafka.network.KafkaHeader;
import io.chubao.joyqueue.broker.kafka.network.KafkaPayloadCodec;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * EndTxnCodec
 *
 * author: gaohaoxiang
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