package com.jd.journalq.server.retry.remote.command.codec;

import com.jd.journalq.network.command.CommandType;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.server.retry.remote.command.GetRetryCount;
import io.netty.buffer.ByteBuf;

/**
 * Created by chengzhiliang on 2018/9/17.
 */
public class GetRetryCountCodec implements PayloadCodec<JMQHeader, GetRetryCount>, Type {
    @Override
    public Object decode(JMQHeader header, ByteBuf buffer) throws Exception {
        if (buffer == null) {
            return null;
        }
        String topic = Serializer.readString(buffer);
        String app = Serializer.readString(buffer);

        GetRetryCount payload = new GetRetryCount().topic(topic).app(app);
        return payload;
    }

    @Override
    public void encode(GetRetryCount payload, ByteBuf buffer) throws Exception {
        // 1字节主题长度
        Serializer.write(payload.getTopic(), buffer);

        // 1字节应用长度
        Serializer.write(payload.getApp(), buffer);
    }

    @Override
    public int type() {
        return CommandType.GET_RETRY_COUNT;
    }
}
