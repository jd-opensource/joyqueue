package com.jd.joyqueue.broker.jmq2.network.codec;

import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;
import com.jd.joyqueue.broker.jmq2.command.RemoveConsumer;
import com.jd.joyqueue.broker.jmq2.network.JMQ2PayloadCodec;
import com.jd.joyqueue.broker.jmq2.util.Serializer;
import org.joyqueue.network.session.ConsumerId;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Payload;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * removeConsumer
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/27
 */
public class RemoveConsumerCodec implements JMQ2PayloadCodec, Type {
    @Override
    public Object decode(Header header, ByteBuf buffer) throws Exception {
        RemoveConsumer payload = new RemoveConsumer();
        // 1字节消费者ID长度
        payload.setConsumerId(new ConsumerId(Serializer.readString(buffer)));
        return payload;
    }

    @Override
    public void encode(Payload payload, ByteBuf buffer) {

    }

    @Override
    public int type() {
        return JMQ2CommandType.REMOVE_CONSUMER.getCode();
    }
}