package io.chubao.joyqueue.network.codec;

import io.chubao.joyqueue.network.command.HeartbeatRequest;
import io.chubao.joyqueue.network.command.JoyQueueCommandType;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.codec.PayloadCodec;
import io.chubao.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * HeartbeatRequestCodec
 *
 * author: gaohaoxiang
 * date: 2018/12/28
 */
public class HeartbeatRequestCodec implements PayloadCodec<JoyQueueHeader, HeartbeatRequest>, Type {

    @Override
    public HeartbeatRequest decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        return new HeartbeatRequest();
    }

    @Override
    public void encode(HeartbeatRequest payload, ByteBuf buffer) throws Exception {

    }

    @Override
    public int type() {
        return JoyQueueCommandType.HEARTBEAT_REQUEST.getCode();
    }
}