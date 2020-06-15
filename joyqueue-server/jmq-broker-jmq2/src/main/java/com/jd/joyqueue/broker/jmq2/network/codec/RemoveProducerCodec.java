package com.jd.joyqueue.broker.jmq2.network.codec;

import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;
import com.jd.joyqueue.broker.jmq2.command.RemoveProducer;
import com.jd.joyqueue.broker.jmq2.network.JMQ2PayloadCodec;
import com.jd.joyqueue.broker.jmq2.util.Serializer;
import org.joyqueue.network.session.ProducerId;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Payload;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * 移除生产者解码器
 */
public class RemoveProducerCodec implements JMQ2PayloadCodec, Type {

    @Override
    public Object decode(Header header, final ByteBuf in) throws Exception {
        RemoveProducer payload = new RemoveProducer();
        // 1字节生产者ID长度
        payload.setProducerId(new ProducerId(Serializer.readString(in)));

        return payload;
    }

    @Override
    public void encode(Payload payload, ByteBuf buffer) {

    }

    @Override
    public int type() {
        return JMQ2CommandType.REMOVE_PRODUCER.getCode();
    }
}