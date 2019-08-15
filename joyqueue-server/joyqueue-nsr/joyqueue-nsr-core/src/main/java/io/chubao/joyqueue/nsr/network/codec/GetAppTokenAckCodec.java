package io.chubao.joyqueue.nsr.network.codec;

import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.command.Header;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.nsr.network.NsrPayloadCodec;
import io.chubao.joyqueue.nsr.network.command.GetAppTokenAck;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

/**
 * @author wylixiaobin
 * Date: 2019/2/13
 */
public class GetAppTokenAckCodec implements NsrPayloadCodec<GetAppTokenAck>, Type {
    @Override
    public GetAppTokenAck decode(Header header, ByteBuf buffer) throws Exception {
        GetAppTokenAck appTokenAck = new GetAppTokenAck();
        if(buffer.readBoolean()){
            appTokenAck.appToken(Serializer.readAppToken(buffer));
        }
        return appTokenAck;
    }

    @Override
    public void encode(GetAppTokenAck payload, ByteBuf buffer) throws Exception {
        if(null==payload.getAppToken()){
            buffer.writeBoolean(false);
            return;
        }
        buffer.writeBoolean(true);
        Serializer.write(payload.getAppToken(),buffer);
    }

    @Override
    public int type() {
        return NsrCommandType.GET_APP_TOKEN_ACK;
    }
}
