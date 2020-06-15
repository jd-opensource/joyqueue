package com.jd.joyqueue.broker.jmq2.network.codec;

import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;
import com.jd.joyqueue.broker.jmq2.command.ResetConsumeOffset;
import com.jd.joyqueue.broker.jmq2.network.JMQ2PayloadCodec;
import com.jd.joyqueue.broker.jmq2.util.Serializer;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Payload;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

public class ResetConsumeOffsetCodec implements JMQ2PayloadCodec, Type {
    @Override
    public Object decode(Header header, ByteBuf buffer) throws Exception {
        ResetConsumeOffset resetOffset = new ResetConsumeOffset();
        resetOffset.setApp(Serializer.readString(buffer));
        resetOffset.setTopic(Serializer.readString(buffer));
        return resetOffset;
    }

    @Override
    public void encode(Payload payload, ByteBuf buffer) {

    }

    @Override
    public int type() {
        return JMQ2CommandType.RESET_CONSUMER_OFFSET.getCode();
    }
}
