package org.joyqueue.broker.joyqueue0.network.codec;

import org.joyqueue.broker.joyqueue0.Joyqueue0CommandType;
import org.joyqueue.broker.joyqueue0.command.UpdateRetry;
import org.joyqueue.broker.joyqueue0.util.Serializer;
import io.netty.buffer.ByteBuf;
import org.joyqueue.network.transport.codec.PayloadDecoder;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Type;

/**
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/22
 */
public class UpdateRetryCodec implements PayloadDecoder, Type {

    @Override
    public Object decode(Header header, ByteBuf buffer) throws Exception {
        UpdateRetry payload = new UpdateRetry();
        payload.setTopic(Serializer.readString(buffer, 2));
        payload.setApp(Serializer.readString(buffer, 2));
        payload.setUpdateType(buffer.readByte());
        short count = buffer.readShort();
        if (count < 0) {
            count = 0;
        }
        long[] messages = new long[count];
        for (int i = 0; i < count; i++) {
            messages[i] = buffer.readLong();
        }
        payload.setMessages(messages);
        return payload;
    }

    @Override
    public int type() {
        return Joyqueue0CommandType.UPDATE_RETRY.getCode();
    }
}