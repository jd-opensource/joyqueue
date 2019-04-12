package com.jd.journalq.network.codec;

import com.jd.journalq.network.command.AddConnectionResponse;
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
public class AddConnectionAckCodec implements PayloadCodec<JMQHeader, AddConnectionResponse>, Type {

    @Override
    public Object decode(JMQHeader header, ByteBuf buffer) throws Exception {
        AddConnectionResponse addConnectionResponse = new AddConnectionResponse();
        addConnectionResponse.setConnectionId(Serializer.readString(buffer, Serializer.BYTE_SIZE));
        addConnectionResponse.setNotification(Serializer.readString(buffer, Serializer.SHORT_SIZE));
        return addConnectionResponse;
    }

    @Override
    public void encode(AddConnectionResponse payload, ByteBuf buffer) throws Exception {
        Serializer.write(payload.getConnectionId(), buffer, Serializer.BYTE_SIZE);
        Serializer.write(payload.getNotification(), buffer, Serializer.SHORT_SIZE);
    }

    @Override
    public int type() {
        return JMQCommandType.ADD_CONNECTION_RESPONSE.getCode();
    }
}