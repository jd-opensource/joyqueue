package io.chubao.joyqueue.nsr.message.support.network.codec;

import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.codec.PayloadCodec;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.nsr.message.support.network.command.MessengerHeartbeatRequest;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

/**
 * MessengerPublishRequestCodec
 * author: gaohaoxiang
 * date: 2019/8/27
 */
public class MessengerHeartbeatRequestCodec implements PayloadCodec<JoyQueueHeader, MessengerHeartbeatRequest>, Type {

    @Override
    public MessengerHeartbeatRequest decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        return new MessengerHeartbeatRequest();
    }

    @Override
    public void encode(MessengerHeartbeatRequest payload, ByteBuf buffer) throws Exception {
    }

    @Override
    public int type() {
        return NsrCommandType.NSR_MESSENGER_HEARTBEAT_REQUEST;
    }
}