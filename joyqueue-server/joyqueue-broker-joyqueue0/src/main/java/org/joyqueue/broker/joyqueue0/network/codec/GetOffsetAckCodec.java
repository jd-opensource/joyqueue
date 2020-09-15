package org.joyqueue.broker.joyqueue0.network.codec;

import org.joyqueue.broker.joyqueue0.Joyqueue0CommandType;
import org.joyqueue.broker.joyqueue0.command.GetOffsetAck;
import io.netty.buffer.ByteBuf;
import org.joyqueue.network.transport.codec.PayloadEncoder;
import org.joyqueue.network.transport.command.Type;

/**
 * 获取偏移量应答解码器
 */
public class GetOffsetAckCodec implements PayloadEncoder<GetOffsetAck>, Type {

    @Override
    public void encode(final GetOffsetAck payload, final ByteBuf out) throws Exception {
        payload.validate();
        out.writeLong(payload.getOffset());
        out.writeLong(payload.getMaxOffset());
    }

    @Override
    public int type() {
        return Joyqueue0CommandType.GET_OFFSET_ACK.getCode();
    }
}