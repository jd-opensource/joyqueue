package com.jd.joyqueue.broker.protocol.network.codec;

import com.jd.joyqueue.network.transport.codec.Codec;
import com.jd.joyqueue.network.transport.codec.DefaultDecoder;
import com.jd.joyqueue.network.transport.codec.JoyQueueHeader;
import com.jd.joyqueue.network.transport.codec.PayloadCodecFactory;
import com.jd.joyqueue.network.transport.command.Command;
import com.jd.joyqueue.network.transport.command.JoyQueuePayload;
import com.jd.joyqueue.network.transport.exception.TransportException;
import io.netty.buffer.ByteBuf;

/**
 * JoyQueueDecoder
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/6/27
 */
public class JoyQueueDecoder extends DefaultDecoder {

    public JoyQueueDecoder(Codec headerCodec, PayloadCodecFactory payloadCodecFactory) {
        super(headerCodec, payloadCodecFactory);
    }

    @Override
    public Object decode(ByteBuf buffer) throws TransportException.CodecException {
        Command command = (Command) super.decode(buffer);
        if (command != null && command.getPayload() instanceof JoyQueuePayload) {
            fillHeader((JoyQueueHeader) command.getHeader(), (JoyQueuePayload) command.getPayload());
        }
        return command;
    }

    private void fillHeader(JoyQueueHeader header, JoyQueuePayload payload) throws TransportException.CodecException {
        payload.setHeader(header);
    }
}