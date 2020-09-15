package org.joyqueue.broker.joyqueue0.network.codec;

import org.joyqueue.broker.joyqueue0.Joyqueue0CommandType;
import org.joyqueue.broker.joyqueue0.command.GetConsumerHealth;
import org.joyqueue.broker.joyqueue0.network.Joyqueue0PayloadCodec;
import org.joyqueue.broker.joyqueue0.util.Serializer;
import io.netty.buffer.ByteBuf;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Payload;
import org.joyqueue.network.transport.command.Type;

/**
 * 消费者健康检测解码器
 */
public class GetConsumerHealthCodec extends GetHealthDecoder implements Joyqueue0PayloadCodec, Type {

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
        return Joyqueue0CommandType.GET_CONSUMER_HEALTH.getCode();
    }
}