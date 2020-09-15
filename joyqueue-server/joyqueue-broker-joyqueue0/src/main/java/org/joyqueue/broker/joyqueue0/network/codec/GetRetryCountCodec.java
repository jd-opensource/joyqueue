package org.joyqueue.broker.joyqueue0.network.codec;

import org.joyqueue.broker.joyqueue0.Joyqueue0CommandType;
import org.joyqueue.broker.joyqueue0.command.GetRetryCount;
import org.joyqueue.broker.joyqueue0.util.Serializer;
import io.netty.buffer.ByteBuf;
import org.joyqueue.network.transport.codec.PayloadDecoder;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Type;

/**
 * getRetryCountCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/22
 */
public class GetRetryCountCodec implements PayloadDecoder<Header>, Type {

    @Override
    public GetRetryCount decode(Header header, ByteBuf buffer) throws Exception {
        GetRetryCount payload = new GetRetryCount();
        payload.setTopic(Serializer.readString(buffer, 2));
        payload.setApp(Serializer.readString(buffer, 2));
        return payload;
    }

    @Override
    public int type() {
        return Joyqueue0CommandType.GET_RETRY.getCode();
    }
}