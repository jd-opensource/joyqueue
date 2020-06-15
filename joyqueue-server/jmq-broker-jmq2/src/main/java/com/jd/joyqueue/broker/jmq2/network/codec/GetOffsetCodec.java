package com.jd.joyqueue.broker.jmq2.network.codec;

import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;
import com.jd.joyqueue.broker.jmq2.command.GetOffset;
import org.joyqueue.network.transport.codec.PayloadDecoder;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * 获取偏移量解码器
 */
public class GetOffsetCodec implements PayloadDecoder, Type {

    @Override
    public Object decode(Header header, final ByteBuf in) throws Exception {
        GetOffset payload = new GetOffset();
        payload.setOffset(in.readLong());
        payload.setOptimized(in.readByte() == 1);
        return payload;
    }

    @Override
    public int type() {
        return JMQ2CommandType.GET_OFFSET.getCode();
    }
}