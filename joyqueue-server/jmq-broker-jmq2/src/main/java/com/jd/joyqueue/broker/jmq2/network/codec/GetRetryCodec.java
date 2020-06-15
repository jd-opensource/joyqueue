package com.jd.joyqueue.broker.jmq2.network.codec;

import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;
import com.jd.joyqueue.broker.jmq2.command.GetRetry;
import com.jd.joyqueue.broker.jmq2.util.Serializer;
import org.joyqueue.network.transport.codec.PayloadDecoder;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * 获取重试解码器
 */
public class GetRetryCodec implements PayloadDecoder, Type {

    @Override
    public Object decode(Header header, final ByteBuf in) throws Exception {
        GetRetry payload = new GetRetry();
        payload.setTopic(Serializer.readString(in));
        payload.setApp(Serializer.readString(in));
        payload.setCount(in.readShort());
        payload.setStartId(in.readLong());
        return payload;
    }

    @Override
    public int type() {
        return JMQ2CommandType.GET_RETRY.getCode();
    }
}