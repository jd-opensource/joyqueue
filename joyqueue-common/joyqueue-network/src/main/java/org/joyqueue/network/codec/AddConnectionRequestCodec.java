/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.network.codec;

import org.joyqueue.network.command.AddConnectionRequest;
import org.joyqueue.network.command.JoyQueueCommandType;
import org.joyqueue.network.serializer.Serializer;
import org.joyqueue.network.session.ClientId;
import org.joyqueue.network.session.Language;
import org.joyqueue.network.transport.codec.JoyQueueHeader;
import org.joyqueue.network.transport.codec.PayloadCodec;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * AddConnectionRequestCodec
 *
 * author: gaohaoxiang
 * date: 2018/11/29
 */
public class AddConnectionRequestCodec implements PayloadCodec<JoyQueueHeader, AddConnectionRequest>, Type {

    @Override
    public AddConnectionRequest decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
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
        return JoyQueueCommandType.ADD_CONNECTION_REQUEST.getCode();
    }
}