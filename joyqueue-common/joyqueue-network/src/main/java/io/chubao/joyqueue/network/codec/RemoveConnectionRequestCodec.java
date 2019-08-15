package io.chubao.joyqueue.network.codec;

import io.chubao.joyqueue.network.command.JoyQueueCommandType;
import io.chubao.joyqueue.network.command.RemoveConnectionRequest;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.codec.PayloadCodec;
import io.chubao.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * RemoveConnectionRequestCodec
 *
 * author: gaohaoxiang
 * date: 2018/11/30
 */
public class RemoveConnectionRequestCodec implements PayloadCodec<JoyQueueHeader, RemoveConnectionRequest>, Type {

    @Override
    public RemoveConnectionRequest decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        return new RemoveConnectionRequest();
    }

    @Override
    public void encode(RemoveConnectionRequest payload, ByteBuf buffer) throws Exception {
    }

    @Override
    public int type() {
        return JoyQueueCommandType.REMOVE_CONNECTION_REQUEST.getCode();
    }
}