package io.chubao.joyqueue.server.retry.remote.command.codec;

import io.chubao.joyqueue.network.command.CommandType;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.codec.PayloadCodec;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.server.retry.remote.command.GetRetryCount;
import io.netty.buffer.ByteBuf;

/**
 * Created by chengzhiliang on 2018/9/17.
 */
public class GetRetryCountCodec implements PayloadCodec<JoyQueueHeader, GetRetryCount>, Type {
    @Override
    public Object decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
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
