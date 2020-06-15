package com.jd.joyqueue.broker.jmq2.network.codec;

import com.google.common.collect.Lists;
import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;
import com.jd.joyqueue.broker.jmq2.command.PutRetry;
import com.jd.joyqueue.broker.jmq2.util.Serializer;
import org.joyqueue.message.BrokerMessage;
import org.joyqueue.network.transport.codec.PayloadDecoder;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.List;

/**
 * putRetry
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/21
 */
public class PutRetryCodec implements PayloadDecoder, Type {

    @Override
    public Object decode(Header header, ByteBuf buffer) throws Exception {
        PutRetry payload = new PutRetry();
        payload.setTopic(Serializer.readString(buffer, 2));
        payload.setApp(Serializer.readString(buffer, 2));
        payload.setException(Serializer.readString(buffer, 2));

        int count = buffer.readShort();
        List<BrokerMessage> messages = Lists.newArrayListWithCapacity(count);
        for (int i = 0; i < count; i++) {
            messages.add(Serializer.readBrokerMessage(buffer));
        }

        payload.setMessages(messages);
        return payload;
    }

    @Override
    public int type() {
        return JMQ2CommandType.PUT_RETRY.getCode();
    }
}