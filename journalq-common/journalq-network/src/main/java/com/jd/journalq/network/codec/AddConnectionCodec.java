package com.jd.journalq.network.codec;

import com.jd.journalq.network.command.AddConnectionRequest;
import com.jd.journalq.network.command.JMQCommandType;
import com.jd.journalq.network.serializer.Serializer;
import com.jd.journalq.network.session.ClientId;
import com.jd.journalq.network.session.Language;
import com.jd.journalq.network.transport.codec.JMQHeader;
import com.jd.journalq.network.transport.codec.PayloadCodec;
import com.jd.journalq.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * AddConnectionCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/29
 */
public class AddConnectionCodec implements PayloadCodec<JMQHeader, AddConnectionRequest>, Type {

    @Override
    public AddConnectionRequest decode(JMQHeader header, ByteBuf buffer) throws Exception {
        AddConnectionRequest addConnectionRequest = new AddConnectionRequest();
        ClientId clientId = new ClientId();

        addConnectionRequest.setUsername(Serializer.readString(buffer, Serializer.BYTE_SIZE));
        addConnectionRequest.setPassword(Serializer.readString(buffer, Serializer.BYTE_SIZE));
        addConnectionRequest.setApp(Serializer.readString(buffer, Serializer.BYTE_SIZE));
        addConnectionRequest.setToken(Serializer.readString(buffer, Serializer.BYTE_SIZE));
        addConnectionRequest.setRegion(Serializer.readString(buffer, Serializer.BYTE_SIZE));
        addConnectionRequest.setNamespace(Serializer.readString(buffer, Serializer.BYTE_SIZE));
        addConnectionRequest.setLanguage(Language.valueOf(buffer.readByte()));

        clientId.setVersion(Serializer.readString(buffer, Serializer.BYTE_SIZE));
        clientId.setIp(Serializer.readString(buffer, Serializer.BYTE_SIZE));
        clientId.setTime(buffer.readLong());
        clientId.setSequence(buffer.readLong());
        addConnectionRequest.setClientId(clientId);
        return addConnectionRequest;
    }

    @Override
    public void encode(AddConnectionRequest payload, ByteBuf buffer) throws Exception {
        ClientId clientId = payload.getClientId();

        Serializer.write(payload.getUsername(), buffer, Serializer.BYTE_SIZE);
        Serializer.write(payload.getPassword(), buffer, Serializer.BYTE_SIZE);
        Serializer.write(payload.getApp(), buffer, Serializer.BYTE_SIZE);
        Serializer.write(payload.getToken(), buffer, Serializer.BYTE_SIZE);
        Serializer.write(payload.getRegion(), buffer, Serializer.BYTE_SIZE);
        Serializer.write(payload.getNamespace(), buffer, Serializer.BYTE_SIZE);
        buffer.writeByte(payload.getLanguage().ordinal());

        Serializer.write(clientId.getVersion(), buffer, Serializer.BYTE_SIZE);
        Serializer.write(clientId.getIp(), buffer, Serializer.BYTE_SIZE);
        buffer.writeLong(clientId.getTime());
        buffer.writeLong(clientId.getSequence());
    }

    @Override
    public int type() {
        return JMQCommandType.ADD_CONNECTION_REQUEST.getCode();
    }
}