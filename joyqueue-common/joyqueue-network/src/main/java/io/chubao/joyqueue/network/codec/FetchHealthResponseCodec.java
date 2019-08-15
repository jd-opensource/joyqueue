package io.chubao.joyqueue.network.codec;

import io.chubao.joyqueue.network.command.FetchHealthResponse;
import io.chubao.joyqueue.network.command.JoyQueueCommandType;
import io.chubao.joyqueue.network.transport.codec.JoyQueueHeader;
import io.chubao.joyqueue.network.transport.codec.PayloadCodec;
import io.chubao.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * FetchHealthResponseCodec
 *
 * author: gaohaoxiang
 * date: 2018/12/28
 */
public class FetchHealthResponseCodec implements PayloadCodec<JoyQueueHeader, FetchHealthResponse>, Type {

    @Override
    public Object decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        double point = buffer.readDouble();
        FetchHealthResponse fetchHealthResponse = new FetchHealthResponse();
        fetchHealthResponse.setPoint(point);
        return fetchHealthResponse;
    }

    @Override
    public void encode(FetchHealthResponse payload, ByteBuf buffer) throws Exception {
        buffer.writeDouble(payload.getPoint());
    }

    @Override
    public int type() {
        return JoyQueueCommandType.FETCH_HEALTH_RESPONSE.getCode();
    }
}