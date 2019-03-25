package com.jd.journalq.network.codec;

import com.jd.journalq.network.command.AddConnectionAck;
import com.jd.journalq.network.command.JMQCommandType;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * AddConnectionAckCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/29
 */
public class AddConnectionAckCodec implements PayloadCodec<JMQHeader, AddConnectionAck>, Type {

    @Override
    public Object decode(JMQHeader header, ByteBuf buffer) throws Exception {
        AddConnectionAck addConnectionAck = new AddConnectionAck();
        addConnectionAck.setConnectionId(Serializer.readString(buffer, Serializer.BYTE_SIZE));
        addConnectionAck.setNotification(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        return addConnectionAck;
    }

    @Override
    public void encode(AddConnectionAck payload, ByteBuf buffer) throws Exception {
        Serializer.write(payload.getConnectionId(), buffer, Serializer.BYTE_SIZE);
        Serializer.write(payload.getNotification(), buffer, Serializer.SHORT_SIZE);
    }

    @Override
    public int type() {
        return JMQCommandType.ADD_CONNECTION_ACK.getCode();
    }
}