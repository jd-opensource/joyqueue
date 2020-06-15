package com.jd.joyqueue.broker.jmq2.network.codec;

import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;
import com.jd.joyqueue.broker.jmq2.command.GetRetryCount;
import com.jd.joyqueue.broker.jmq2.util.Serializer;
import org.joyqueue.network.transport.codec.PayloadDecoder;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

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
        return JMQ2CommandType.GET_RETRY.getCode();
    }
}