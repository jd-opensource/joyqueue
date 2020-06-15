package com.jd.joyqueue.broker.jmq2.network.codec;

import com.jd.joyqueue.broker.jmq2.command.GetHealth;
import com.jd.joyqueue.broker.jmq2.util.Serializer;
import io.netty.buffer.ByteBuf;

public class GetHealthDecoder {

    public GetHealth decode(final GetHealth payload, final ByteBuf in) throws Exception {
        // 应用
        payload.setApp(Serializer.readString(in));
        //主题
        payload.setTopic(Serializer.readString(in));
        // 1字节数据中心
        payload.setDataCenter(in.readByte());
        return payload;
    }
}