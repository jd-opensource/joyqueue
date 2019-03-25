package com.jd.journalq.server.retry.remote.command.codec;

import com.jd.journalq.common.network.command.CommandType;
import com.jd.journalq.common.network.serializer.Serializer;
import com.jd.journalq.common.network.transport.codec.JMQHeader;
import com.jd.journalq.common.network.transport.codec.PayloadCodec;
import com.jd.journalq.common.network.transport.command.Type;
import com.jd.journalq.server.retry.remote.command.GetRetry;
import io.netty.buffer.ByteBuf;

/**
 * Created by chengzhiliang on 2018/9/17.
 */
public class GetRetryCodec implements PayloadCodec<JMQHeader, GetRetry>, Type {
    @Override
    public Object decode(JMQHeader header, ByteBuf buffer) throws Exception {
        if (buffer == null) {
            return null;
        }
        String topic = Serializer.readString(buffer);
        String app = Serializer.readString(buffer);
        short count = buffer.readShort();
        long startId = buffer.readLong();

        GetRetry getRetryPayload = new GetRetry().topic(topic).app(app).count(count).startId(startId);
        return getRetryPayload;
    }

    @Override
    public void encode(GetRetry payload, ByteBuf buffer) throws Exception {
        // 1字节主题长度
        Serializer.write(payload.getTopic(), buffer);

        // 1字节应用长度
        Serializer.write(payload.getApp(), buffer);

        // 2字节个数
        buffer.writeShort(payload.getCount());
        buffer.writeLong(payload.getStartId());
    }

    @Override
    public int type() {
        return CommandType.GET_RETRY;
    }
}
