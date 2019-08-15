package io.chubao.joyqueue.server.retry.remote.command.codec;

import io.chubao.joyqueue.network.command.BooleanAck;
import io.chubao.joyqueue.network.command.CommandType;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.codec.PayloadCodec;
import io.chubao.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * Created by chengzhiliang on 2018/10/12.
 */
public class BooleanAckCodec implements PayloadCodec<JoyQueueHeader, BooleanAck>, Type {

    @Override
    public Object decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        // 布尔应答不解析消息体
        return null;
    }

    @Override
    public void encode(BooleanAck payload, ByteBuf buffer) throws Exception {
        // 布尔应答编码消息体
    }

    @Override
    public int type() {
        return CommandType.BOOLEAN_ACK;
    }
}
