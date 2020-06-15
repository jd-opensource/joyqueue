package com.jd.joyqueue.broker.jmq2.network.codec;

import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;
import com.jd.joyqueue.broker.jmq2.command.AddProducer;
import com.jd.joyqueue.broker.jmq2.network.JMQ2PayloadCodec;
import com.jd.joyqueue.broker.jmq2.util.Serializer;
import org.joyqueue.domain.TopicName;
import org.joyqueue.network.session.ProducerId;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Payload;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * 添加生产者解码器
 */
public class AddProducerCodec implements JMQ2PayloadCodec, Type {

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
        return JMQ2CommandType.ADD_PRODUCER.getCode();
    }
}