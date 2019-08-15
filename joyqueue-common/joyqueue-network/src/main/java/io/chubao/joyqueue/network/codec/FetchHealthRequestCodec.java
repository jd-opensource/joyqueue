package io.chubao.joyqueue.network.codec;

import io.chubao.joyqueue.network.command.FetchHealthRequest;
import io.chubao.joyqueue.network.command.JoyQueueCommandType;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.codec.PayloadCodec;
import io.chubao.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * FetchHealthRequestCodec
 *
 * author: gaohaoxiang
 * date: 2018/12/28
 */
public class FetchHealthRequestCodec implements PayloadCodec<JoyQueueHeader, FetchHealthRequest>, Type {

    @Override
    public Object decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        return new FetchHealthRequest();
    }

    @Override
    public void encode(FetchHealthRequest payload, ByteBuf buffer) throws Exception {

    }

    @Override
    public int type() {
        return JoyQueueCommandType.FETCH_HEALTH_REQUEST.getCode();
    }
}