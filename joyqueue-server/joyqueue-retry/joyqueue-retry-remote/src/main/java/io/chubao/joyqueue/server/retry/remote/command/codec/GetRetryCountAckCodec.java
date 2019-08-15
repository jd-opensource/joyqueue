package io.chubao.joyqueue.server.retry.remote.command.codec;

import io.chubao.joyqueue.network.command.CommandType;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.codec.PayloadCodec;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.server.retry.remote.command.GetRetryCountAck;
import io.netty.buffer.ByteBuf;

/**
 * Created by chengzhiliang on 2018/9/17.
 */
public class GetRetryCountAckCodec implements PayloadCodec<JoyQueueHeader, GetRetryCountAck>, Type {
    @Override
    public Object decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        if (buffer == null) {
            return null;
        }

        String topic = Serializer.readString(buffer);
        String app = Serializer.readString(buffer);
        int count = buffer.readInt();

        GetRetryCountAck getRetryCountAckPayload = new GetRetryCountAck().topic(topic).app(app).count(count);
        return getRetryCountAckPayload;
    }

    @Override
    public void encode(GetRetryCountAck payload, ByteBuf buffer) throws Exception {
        // 1字节主题长度
        Serializer.write(payload.getTopic(), buffer);

        // 1字节应用长度
        Serializer.write(payload.getApp(), buffer);

        // 4字节条数
        buffer.writeInt(payload.getCount());
    }

    @Override
    public int type() {
        return CommandType.GET_RETRY_COUNT_ACK;
    }
}
