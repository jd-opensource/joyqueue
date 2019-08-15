package io.chubao.joyqueue.nsr.network.codec;

import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.command.Header;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.nsr.network.NsrPayloadCodec;
import io.chubao.joyqueue.nsr.network.command.GetBrokerByRetryType;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

/**
 * @author wylixiaobin
 * Date: 2019/1/29
 */
public class GetBrokerByRetryTypeCodec implements NsrPayloadCodec<GetBrokerByRetryType>, Type {
    @Override
    public GetBrokerByRetryType decode(Header header, ByteBuf buffer) throws Exception {
        return new GetBrokerByRetryType().retryType(Serializer.readString(buffer));
    }

    @Override
    public void encode(GetBrokerByRetryType payload, ByteBuf buffer) throws Exception {
        Serializer.write(payload.getRetryType(),buffer);
    }

    @Override
    public int type() {
        return NsrCommandType.GET_BROKER_BY_RETRYTYPE;
    }
}
