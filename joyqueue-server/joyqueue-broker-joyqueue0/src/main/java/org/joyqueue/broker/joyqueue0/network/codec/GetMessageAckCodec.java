package org.joyqueue.broker.joyqueue0.network.codec;

import org.joyqueue.broker.joyqueue0.Joyqueue0CommandType;
import org.joyqueue.broker.joyqueue0.command.GetMessageAck;
import org.joyqueue.broker.joyqueue0.network.Joyqueue0Header;
import org.joyqueue.broker.joyqueue0.network.Joyqueue0PayloadCodec;
import org.joyqueue.broker.joyqueue0.util.Serializer;
import io.netty.buffer.ByteBuf;
import org.apache.commons.collections.CollectionUtils;
import org.joyqueue.message.BrokerMessage;
import org.joyqueue.network.transport.command.Type;

import java.util.List;

/**
 * getMessageAck
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/21
 */
public class GetMessageAckCodec implements Joyqueue0PayloadCodec<GetMessageAck>, Type {

    @Override
    public void encode(GetMessageAck payload, ByteBuf buffer) throws Exception {
        List<BrokerMessage> messages = payload.getMessages();
        if (CollectionUtils.isEmpty(messages)) {
            buffer.writeShort(0);
        } else {
            Serializer.writeMessage(messages, buffer);
        }
    }

    @Override
    public Object decode(Joyqueue0Header header, ByteBuf buffer) throws Exception {
        return null;
    }

    @Override
    public int type() {
        return Joyqueue0CommandType.GET_MESSAGE_ACK.getCode();
    }
}