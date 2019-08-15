package io.chubao.joyqueue.network.codec;

import io.chubao.joyqueue.network.command.CommandType;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.codec.PayloadCodec;
import io.chubao.joyqueue.network.command.BooleanAck;
import io.chubao.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * BooleanAckCodec
 *
 * author: gaohaoxiang
 * date: 2018/11/30
 */
public class BooleanAckCodec implements PayloadCodec<JoyQueueHeader, BooleanAck>, Type {

    @Override
    public Object decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        return null;
    }

    @Override
    public void encode(BooleanAck payload, ByteBuf buffer) throws Exception {
    }

    @Override
    public int type() {
        return CommandType.BOOLEAN_ACK;
    }
}