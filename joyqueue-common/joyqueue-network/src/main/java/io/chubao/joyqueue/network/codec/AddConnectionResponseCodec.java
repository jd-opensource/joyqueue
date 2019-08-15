package io.chubao.joyqueue.network.codec;

import io.chubao.joyqueue.network.command.JoyQueueCommandType;
import io.chubao.joyqueue.network.command.AddConnectionResponse;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.codec.PayloadCodec;
import io.chubao.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * AddConnectionResponseCodec
 *
 * author: gaohaoxiang
 * date: 2018/11/29
 */
public class AddConnectionResponseCodec implements PayloadCodec<JoyQueueHeader, AddConnectionResponse>, Type {

    @Override
    public Object decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
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
        return JoyQueueCommandType.ADD_CONNECTION_RESPONSE.getCode();
    }
}