package org.joyqueue.broker.joyqueue0.network.codec;

import org.joyqueue.broker.joyqueue0.Joyqueue0CommandType;
import org.joyqueue.broker.joyqueue0.command.GetRetry;
import org.joyqueue.broker.joyqueue0.util.Serializer;
import io.netty.buffer.ByteBuf;
import org.joyqueue.network.transport.codec.PayloadDecoder;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Type;

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
        return Joyqueue0CommandType.GET_RETRY.getCode();
    }
}