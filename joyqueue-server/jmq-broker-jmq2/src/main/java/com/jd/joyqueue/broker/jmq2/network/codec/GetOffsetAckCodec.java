package com.jd.joyqueue.broker.jmq2.network.codec;

import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;
import com.jd.joyqueue.broker.jmq2.command.GetOffsetAck;
import org.joyqueue.network.transport.codec.PayloadEncoder;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

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
        return JMQ2CommandType.GET_OFFSET_ACK.getCode();
    }
}