package io.chubao.joyqueue.network.codec;

import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.network.command.JoyQueueCommandType;
import io.chubao.joyqueue.network.command.ProduceMessageCommitResponse;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.codec.PayloadCodec;
import io.chubao.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * ProduceMessageCommitResponseCodec
 *
 * author: gaohaoxiang
 * date: 2018/12/19
 */
public class ProduceMessageCommitResponseCodec implements PayloadCodec<JoyQueueHeader, ProduceMessageCommitResponse>, Type {

    @Override
    public ProduceMessageCommitResponse decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        ProduceMessageCommitResponse produceMessageCommitResponse = new ProduceMessageCommitResponse();
        produceMessageCommitResponse.setCode(JoyQueueCode.valueOf(buffer.readInt()));
        return produceMessageCommitResponse;
    }

    @Override
    public void encode(ProduceMessageCommitResponse payload, ByteBuf buffer) throws Exception {
        buffer.writeInt(payload.getCode().getCode());
    }

    @Override
    public int type() {
        return JoyQueueCommandType.PRODUCE_MESSAGE_COMMIT_RESPONSE.getCode();
    }
}