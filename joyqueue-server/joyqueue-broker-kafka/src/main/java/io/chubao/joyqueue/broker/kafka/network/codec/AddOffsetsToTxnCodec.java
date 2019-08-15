package io.chubao.joyqueue.broker.kafka.network.codec;

import io.chubao.joyqueue.broker.kafka.KafkaCommandType;
import io.chubao.joyqueue.broker.kafka.command.AddOffsetsToTxnRequest;
import io.chubao.joyqueue.broker.kafka.command.AddOffsetsToTxnResponse;
import io.chubao.joyqueue.broker.kafka.network.KafkaHeader;
import io.chubao.joyqueue.broker.kafka.network.KafkaPayloadCodec;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * AddOffsetsToTxnCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/4
 */
public class AddOffsetsToTxnCodec implements KafkaPayloadCodec<AddOffsetsToTxnResponse>, Type {

    @Override
    public AddOffsetsToTxnRequest decode(KafkaHeader header, ByteBuf buffer) throws Exception {
        String transactionId = Serializer.readString(buffer, Serializer.SHORT_SIZE);
        long producerId = buffer.readLong();
        short producerEpoch = buffer.readShort();
        String groupId = Serializer.readString(buffer, Serializer.SHORT_SIZE);

        AddOffsetsToTxnRequest addOffsetsToTxnRequest = new AddOffsetsToTxnRequest();
        addOffsetsToTxnRequest.setTransactionId(transactionId);
        addOffsetsToTxnRequest.setProducerId(producerId);
        addOffsetsToTxnRequest.setProducerEpoch(producerEpoch);
        addOffsetsToTxnRequest.setGroupId(groupId);
        return addOffsetsToTxnRequest;
    }

    @Override
    public void encode(AddOffsetsToTxnResponse payload, ByteBuf buffer) throws Exception {
        buffer.writeInt(payload.getThrottleTimeMs());
        buffer.writeShort(payload.getCode());
    }

    @Override
    public int type() {
        return KafkaCommandType.ADD_OFFSETS_TO_TXN.getCode();
    }
}