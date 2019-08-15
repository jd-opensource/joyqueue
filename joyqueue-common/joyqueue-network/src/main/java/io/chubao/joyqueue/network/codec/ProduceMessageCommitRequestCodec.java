package io.chubao.joyqueue.network.codec;

import io.chubao.joyqueue.network.command.JoyQueueCommandType;
import io.chubao.joyqueue.network.command.ProduceMessageCommitRequest;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.codec.PayloadCodec;
import io.chubao.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * ProduceMessageCommitRequestCodec
 *
 * author: gaohaoxiang
 * date: 2018/12/19
 */
public class ProduceMessageCommitRequestCodec implements PayloadCodec<JoyQueueHeader, ProduceMessageCommitRequest>, Type {

    @Override
    public ProduceMessageCommitRequest decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        ProduceMessageCommitRequest produceMessageCommitRequest = new ProduceMessageCommitRequest();
        produceMessageCommitRequest.setTopic(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        produceMessageCommitRequest.setApp(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        produceMessageCommitRequest.setTxId(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        return produceMessageCommitRequest;
    }

    @Override
    public void encode(ProduceMessageCommitRequest payload, ByteBuf buffer) throws Exception {
        Serializer.write(payload.getTopic(), buffer, Serializer.SHORT_SIZE);
        Serializer.write(payload.getApp(), buffer, Serializer.SHORT_SIZE);
        Serializer.write(payload.getTxId(), buffer, Serializer.SHORT_SIZE);
    }

    @Override
    public int type() {
        return JoyQueueCommandType.PRODUCE_MESSAGE_COMMIT_REQUEST.getCode();
    }
}