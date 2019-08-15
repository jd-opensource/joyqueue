package io.chubao.joyqueue.network.codec;

import io.chubao.joyqueue.network.command.JoyQueueCommandType;
import io.chubao.joyqueue.network.command.ProduceMessagePrepareRequest;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.codec.PayloadCodec;
import io.chubao.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * ProduceMessagePrepareRequestCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/19
 */
public class ProduceMessagePrepareRequestCodec implements PayloadCodec<JoyQueueHeader, ProduceMessagePrepareRequest>, Type {

    @Override
    public ProduceMessagePrepareRequest decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        ProduceMessagePrepareRequest produceMessagePrepareRequest = new ProduceMessagePrepareRequest();
        produceMessagePrepareRequest.setTopic(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        produceMessagePrepareRequest.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        produceMessagePrepareRequest.setSequence(buffer.readLong());
        produceMessagePrepareRequest.setTransactionId(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        produceMessagePrepareRequest.setTimeout(buffer.readInt());
        return produceMessagePrepareRequest;
    }

    @Override
    public void encode(ProduceMessagePrepareRequest payload, ByteBuf buffer) throws Exception {
        Serializer.write(payload.getTopic(), buffer, Serializer.SHORT_SIZE);
        Serializer.write(payload.getApp(), buffer, Serializer.SHORT_SIZE);
        buffer.writeLong(payload.getSequence());
        Serializer.write(payload.getTransactionId(), buffer, Serializer.SHORT_SIZE);
        buffer.writeInt(payload.getTimeout());
    }

    @Override
    public int type() {
        return JoyQueueCommandType.PRODUCE_MESSAGE_PREPARE_REQUEST.getCode();
    }
}