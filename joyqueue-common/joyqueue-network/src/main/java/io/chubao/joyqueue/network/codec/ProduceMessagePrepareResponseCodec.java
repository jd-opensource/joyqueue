package io.chubao.joyqueue.network.codec;

import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.network.command.JoyQueueCommandType;
import io.chubao.joyqueue.network.command.ProduceMessagePrepareResponse;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.codec.PayloadCodec;
import io.chubao.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * ProduceMessagePrepareResponseCodec
 *
 * author: gaohaoxiang
 * date: 2018/12/19
 */
public class ProduceMessagePrepareResponseCodec implements PayloadCodec<JoyQueueHeader, ProduceMessagePrepareResponse>, Type {

    @Override
    public ProduceMessagePrepareResponse decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        ProduceMessagePrepareResponse produceMessagePrepareResponse = new ProduceMessagePrepareResponse();
        produceMessagePrepareResponse.setTxId(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        produceMessagePrepareResponse.setCode(JoyQueueCode.valueOf(buffer.readInt()));
        return produceMessagePrepareResponse;
    }

    @Override
    public void encode(ProduceMessagePrepareResponse payload, ByteBuf buffer) throws Exception {
        Serializer.write(payload.getTxId(), buffer, Serializer.SHORT_SIZE);
        buffer.writeInt(payload.getCode().getCode());
    }

    @Override
    public int type() {
        return JoyQueueCommandType.PRODUCE_MESSAGE_PREPARE_RESPONSE.getCode();
    }
}