package org.joyqueue.broker.joyqueue0.network.codec;

import org.joyqueue.broker.joyqueue0.Joyqueue0CommandType;
import org.joyqueue.broker.joyqueue0.command.AddProducer;
import org.joyqueue.broker.joyqueue0.network.Joyqueue0PayloadCodec;
import org.joyqueue.broker.joyqueue0.util.Serializer;
import io.netty.buffer.ByteBuf;
import org.joyqueue.domain.TopicName;
import org.joyqueue.network.session.ProducerId;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Payload;
import org.joyqueue.network.transport.command.Type;

/**
 * 添加生产者解码器
 */
public class AddProducerCodec implements Joyqueue0PayloadCodec, Type {

    @Override
    public Object decode(Header header, final ByteBuf in) throws Exception {
        AddProducer payload = new AddProducer();
        // 1字节生产者ID长度
        payload.setProducerId(new ProducerId(Serializer.readString(in)));

        // 1字节主题长度
        payload.setTopic(TopicName.parse(Serializer.readString(in)));
        return payload;
    }

    @Override
    public void encode(Payload payload, ByteBuf buffer) {

    }

    @Override
    public int type() {
        return Joyqueue0CommandType.ADD_PRODUCER.getCode();
    }
}