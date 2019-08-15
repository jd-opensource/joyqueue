package io.chubao.joyqueue.network.codec;

import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.network.command.JoyQueueCommandType;
import io.chubao.joyqueue.network.command.ProduceMessageRollbackResponse;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.codec.PayloadCodec;
import io.chubao.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * ProduceMessageRollbackResponseCodec
 *
 * author: gaohaoxiang
 * date: 2018/12/19
 */
public class ProduceMessageRollbackResponseCodec implements PayloadCodec<JoyQueueHeader, ProduceMessageRollbackResponse>, Type {

    @Override
    public ProduceMessageRollbackResponse decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        ProduceMessageRollbackResponse produceMessageRollbackResponse = new ProduceMessageRollbackResponse();
        produceMessageRollbackResponse.setCode(JoyQueueCode.valueOf(buffer.readInt()));
        return produceMessageRollbackResponse;
    }

    @Override
    public void encode(ProduceMessageRollbackResponse payload, ByteBuf buffer) throws Exception {
        buffer.writeInt(payload.getCode().getCode());
    }

    @Override
    public int type() {
        return JoyQueueCommandType.PRODUCE_MESSAGE_ROLLBACK_RESPONSE.getCode();
    }
}