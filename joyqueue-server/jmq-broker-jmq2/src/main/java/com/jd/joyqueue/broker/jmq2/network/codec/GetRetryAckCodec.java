package com.jd.joyqueue.broker.jmq2.network.codec;

import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;
import com.jd.joyqueue.broker.jmq2.command.GetRetryAck;
import com.jd.joyqueue.broker.jmq2.util.Serializer;
import org.joyqueue.message.BrokerMessage;
import org.joyqueue.network.transport.codec.PayloadEncoder;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * 获取重试应答编码器
 */
public class GetRetryAckCodec implements PayloadEncoder<GetRetryAck>, Type {

    @Override
    public void encode(GetRetryAck payload, ByteBuf buffer) throws Exception {
        BrokerMessage[] messages = payload.getMessages();
        // 2字节条数
        if (messages == null) {
            buffer.writeShort(0);
            return;
        }
        buffer.writeShort(messages.length);

        for (BrokerMessage message : messages) {
            Serializer.write(message, buffer);
        }
    }

    @Override
    public int type() {
        return JMQ2CommandType.GET_RETRY_ACK.getCode();
    }

}