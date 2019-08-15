package io.chubao.joyqueue.nsr.network.codec;

import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.command.Header;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.nsr.network.NsrPayloadCodec;
import io.chubao.joyqueue.nsr.network.command.GetBrokerAck;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

/**
 * @author wylixiaobin
 * Date: 2019/1/27
 */
public class GetBrokerAckCodec implements NsrPayloadCodec<GetBrokerAck>, Type {
    @Override
    public GetBrokerAck decode(Header header, ByteBuf buffer) throws Exception {
        GetBrokerAck brokerAck = new GetBrokerAck();
        if(buffer.readBoolean()){
            brokerAck.broker(Serializer.readBroker(buffer));
        }
        return brokerAck;
    }

    @Override
    public void encode(GetBrokerAck payload, ByteBuf buffer) throws Exception {
        if(null==payload.getBroker()){
            buffer.writeBoolean(false);
            return;
        }
        // 1.boolean
        buffer.writeBoolean(true);
        Serializer.write(payload.getBroker(),buffer);
    }

    @Override
    public int type() {
        return NsrCommandType.GET_BROKER_ACK;
    }
}
