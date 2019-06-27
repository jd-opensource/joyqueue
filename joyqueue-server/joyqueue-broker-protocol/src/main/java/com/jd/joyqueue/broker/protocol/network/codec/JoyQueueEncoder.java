package com.jd.joyqueue.broker.protocol.network.codec;

import com.jd.joyqueue.network.transport.codec.Codec;
import com.jd.joyqueue.network.transport.codec.DefaultEncoder;
import com.jd.joyqueue.network.transport.codec.JoyQueueHeader;
import com.jd.joyqueue.network.transport.codec.PayloadCodecFactory;
import com.jd.joyqueue.network.transport.command.Command;
import com.jd.joyqueue.network.transport.command.JoyQueuePayload;
import com.jd.joyqueue.network.transport.exception.TransportException;
import io.netty.buffer.ByteBuf;

/**
 * JoyQueueEncoder
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/6/27
 */
public class JoyQueueEncoder extends DefaultEncoder {

    public JoyQueueEncoder(Codec headerCodec, PayloadCodecFactory payloadCodecFactory) {
        super(headerCodec, payloadCodecFactory);
    }

    @Override
    public void encode(Object obj, ByteBuf buffer) throws TransportException.CodecException {
        Command command = (Command) obj;
        if (command.getPayload() instanceof JoyQueuePayload) {
            fillHeader((JoyQueueHeader) command.getHeader(), (JoyQueuePayload) command.getPayload());
        }
        command.getHeader().setVersion(JoyQueueHeader.CURRENT_VERSION);
        super.encode(obj, buffer);
    }

    public void fillHeader(JoyQueueHeader header, JoyQueuePayload payload) {
        payload.setHeader(header);
    }
}