package com.jd.journalq.common.network.codec;

import com.jd.journalq.common.network.command.AddConnection;
import com.jd.journalq.common.network.command.JMQCommandType;
import com.jd.journalq.common.network.serializer.Serializer;
import com.jd.journalq.common.network.session.ClientId;
import com.jd.journalq.common.network.session.Language;
import com.jd.journalq.common.network.transport.codec.JMQHeader;
import com.jd.journalq.common.network.transport.codec.PayloadCodec;
import com.jd.journalq.common.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * AddConnectionCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/11/29
 */
public class AddConnectionCodec implements PayloadCodec<JMQHeader, AddConnection>, Type {

    @Override
    public AddConnection decode(JMQHeader header, ByteBuf buffer) throws Exception {
        AddConnection addConnection = new AddConnection();
        ClientId clientId = new ClientId();

        addConnection.setUsername(Serializer.readString(buffer, Serializer.BYTE_SIZE));
        addConnection.setPassword(Serializer.readString(buffer, Serializer.BYTE_SIZE));
        addConnection.setApp(Serializer.readString(buffer, Serializer.BYTE_SIZE));
        addConnection.setToken(Serializer.readString(buffer, Serializer.BYTE_SIZE));
        addConnection.setRegion(Serializer.readString(buffer, Serializer.BYTE_SIZE));
        addConnection.setNamespace(Serializer.readString(buffer, Serializer.BYTE_SIZE));
        addConnection.setLanguage(Language.valueOf(buffer.readByte()));

        clientId.setVersion(Serializer.readString(buffer, Serializer.BYTE_SIZE));
        clientId.setIp(Serializer.readString(buffer, Serializer.BYTE_SIZE));
        clientId.setTime(buffer.readLong());
        clientId.setSequence(buffer.readLong());
        addConnection.setClientId(clientId);
        return addConnection;
    }

    @Override
    public void encode(AddConnection payload, ByteBuf buffer) throws Exception {
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
        return JMQCommandType.ADD_CONNECTION.getCode();
    }
}