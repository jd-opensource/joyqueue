package io.chubao.joyqueue.server.retry.remote.command.codec;

import io.chubao.joyqueue.network.command.CommandType;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.codec.PayloadCodec;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.server.retry.remote.command.GetRetry;
import io.netty.buffer.ByteBuf;

/**
 * Created by chengzhiliang on 2018/9/17.
 */
public class GetRetryCodec implements PayloadCodec<JoyQueueHeader, GetRetry>, Type {
    @Override
    public Object decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
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
