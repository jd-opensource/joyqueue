package com.jd.joyqueue.broker.jmq2.network.codec;

import com.jd.joyqueue.broker.jmq2.JMQ2CommandType;
import com.jd.joyqueue.broker.jmq2.command.GetMessageAck;
import com.jd.joyqueue.broker.jmq2.network.JMQ2Header;
import com.jd.joyqueue.broker.jmq2.network.JMQ2PayloadCodec;
import com.jd.joyqueue.broker.jmq2.util.Serializer;
import org.joyqueue.message.BrokerMessage;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * getMessageAck
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/8/21
 */
public class GetMessageAckCodec implements JMQ2PayloadCodec<GetMessageAck>, Type {

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
    public Object decode(JMQ2Header header, ByteBuf buffer) throws Exception {
        return null;
    }

    @Override
    public int type() {
        return JMQ2CommandType.GET_MESSAGE_ACK.getCode();
    }
}