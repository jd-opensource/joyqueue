package org.joyqueue.broker.joyqueue0.network.codec;

import org.joyqueue.broker.joyqueue0.Joyqueue0CommandType;
import org.joyqueue.broker.joyqueue0.command.ResetConsumeOffset;
import org.joyqueue.broker.joyqueue0.network.Joyqueue0PayloadCodec;
import org.joyqueue.broker.joyqueue0.util.Serializer;
import io.netty.buffer.ByteBuf;
import org.joyqueue.network.transport.command.Header;
import org.joyqueue.network.transport.command.Payload;
import org.joyqueue.network.transport.command.Type;

public class ResetConsumeOffsetCodec implements Joyqueue0PayloadCodec, Type {
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
        return Joyqueue0CommandType.RESET_CONSUMER_OFFSET.getCode();
    }
}
