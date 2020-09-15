package org.joyqueue.broker.joyqueue0.network.codec;

import org.joyqueue.broker.joyqueue0.Joyqueue0CommandType;
import org.joyqueue.broker.joyqueue0.command.GetRetryAck;
import org.joyqueue.broker.joyqueue0.util.Serializer;
import io.netty.buffer.ByteBuf;
import org.joyqueue.message.BrokerMessage;
import org.joyqueue.network.transport.codec.PayloadEncoder;
import org.joyqueue.network.transport.command.Type;

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
        return Joyqueue0CommandType.GET_RETRY_ACK.getCode();
    }

}