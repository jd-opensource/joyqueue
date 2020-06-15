package com.jd.joyqueue.broker.jmq2.network.codec;

import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;
import com.jd.joyqueue.broker.jmq2.command.GetRetryCountAck;
import com.jd.joyqueue.broker.jmq2.util.Serializer;
import org.joyqueue.network.transport.codec.PayloadEncoder;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

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
        return JMQ2CommandType.GET_RETRY_COUNT_ACK.getCode();
    }
}