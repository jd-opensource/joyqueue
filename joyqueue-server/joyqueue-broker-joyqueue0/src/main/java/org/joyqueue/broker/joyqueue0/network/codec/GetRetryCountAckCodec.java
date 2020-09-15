package org.joyqueue.broker.joyqueue0.network.codec;

import org.joyqueue.broker.joyqueue0.Joyqueue0CommandType;
import org.joyqueue.broker.joyqueue0.command.GetRetryCountAck;
import org.joyqueue.broker.joyqueue0.util.Serializer;
import io.netty.buffer.ByteBuf;
import org.joyqueue.network.transport.codec.PayloadEncoder;
import org.joyqueue.network.transport.command.Type;

/**
 * 获取重试次数应答编码器
 */
public class GetRetryCountAckCodec implements PayloadEncoder<GetRetryCountAck>, Type {

    @Override
    public void encode(GetRetryCountAck payload, ByteBuf out) throws Exception {
        // 1字节主题长度
        Serializer.write(payload.getTopic(), out);
        // 1字节应用长度
        Serializer.write(payload.getApp(), out);
        // 4字节重试条数
        out.writeInt(payload.getCount());
    }

    @Override
    public int type() {
        return Joyqueue0CommandType.GET_RETRY_COUNT_ACK.getCode();
    }
}