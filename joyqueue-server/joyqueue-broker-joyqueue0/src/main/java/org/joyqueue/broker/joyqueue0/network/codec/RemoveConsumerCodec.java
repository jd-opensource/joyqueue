package org.joyqueue.broker.joyqueue0.network.codec;

import org.joyqueue.broker.joyqueue0.Joyqueue0CommandType;
import org.joyqueue.broker.joyqueue0.command.RemoveConsumer;
import org.joyqueue.broker.joyqueue0.network.Joyqueue0PayloadCodec;
import org.joyqueue.broker.joyqueue0.util.Serializer;
import io.netty.buffer.ByteBuf;
import org.joyqueue.network.session.ConsumerId;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Payload;
import org.joyqueue.network.transport.command.Type;

/**
 * removeConsumer
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/27
 */
public class RemoveConsumerCodec implements Joyqueue0PayloadCodec, Type {
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
        return Joyqueue0CommandType.REMOVE_CONSUMER.getCode();
    }
}