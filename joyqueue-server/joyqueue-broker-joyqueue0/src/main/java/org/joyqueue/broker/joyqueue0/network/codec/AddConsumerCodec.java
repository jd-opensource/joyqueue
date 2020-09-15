package org.joyqueue.broker.joyqueue0.network.codec;

import org.joyqueue.broker.joyqueue0.Joyqueue0CommandType;
import org.joyqueue.broker.joyqueue0.command.AddConsumer;
import org.joyqueue.broker.joyqueue0.network.Joyqueue0PayloadCodec;
import org.joyqueue.broker.joyqueue0.util.Serializer;
import io.netty.buffer.ByteBuf;
import org.joyqueue.domain.TopicName;
import org.joyqueue.network.session.ConsumerId;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Payload;
import org.joyqueue.network.transport.command.Type;

/**
 * 添加消费者解码器
 */
public class AddConsumerCodec implements Joyqueue0PayloadCodec, Type {

    @Override
    public AddConsumer decode(Header header, final ByteBuf in) throws Exception {
        AddConsumer payload = new AddConsumer();
        // 1字节消费者ID长度
        payload.setConsumerId(new ConsumerId(Serializer.readString(in)));
        // 1字节主题长度
        payload.setTopic(TopicName.parse(Serializer.readString(in)));
        // 2字节选择器长度
        payload.setSelector(Serializer.readString(in, 2));

        return payload;
    }

    @Override
    public void encode(Payload payload, ByteBuf buffer) {

    }

    @Override
    public int type() {
        return Joyqueue0CommandType.ADD_CONSUMER.getCode();
    }
}