package com.jd.joyqueue.broker.jmq2.network.codec;

import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;
import com.jd.joyqueue.broker.jmq2.command.GetConsumerHealth;
import com.jd.joyqueue.broker.jmq2.network.JMQ2PayloadCodec;
import com.jd.joyqueue.broker.jmq2.util.Serializer;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Payload;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * 消费者健康检测解码器
 */
public class GetConsumerHealthCodec extends GetHealthDecoder implements JMQ2PayloadCodec, Type {

    @Override
    public Object decode(Header header, final ByteBuf in) throws Exception {
        GetConsumerHealth payload = new GetConsumerHealth();
        super.decode(payload, in);
        payload.setConsumerId(Serializer.readString(in));
        return payload;
    }

    @Override
    public void encode(Payload payload, ByteBuf buffer) throws Exception {

    }

    @Override
    public int type() {
        return JMQ2CommandType.GET_CONSUMER_HEALTH.getCode();
    }
}