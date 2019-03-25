package com.jd.journalq.nsr.network.codec;

import com.jd.journalq.network.transport.command.Header;
import com.jd.journalq.network.transport.command.Type;
import com.jd.journalq.nsr.network.NsrPayloadCodec;
import com.jd.journalq.nsr.network.command.GetReplicaByBroker;
import com.jd.journalq.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

/**
 * @author wylixiaobin
 * Date: 2019/1/29
 */
public class GetReplicaByBrokerCodec implements NsrPayloadCodec<GetReplicaByBroker>, Type {
    @Override
    public GetReplicaByBroker decode(Header header, ByteBuf buffer) throws Exception {
        return new GetReplicaByBroker().brokerId(buffer.readInt());
    }

    @Override
    public void encode(GetReplicaByBroker payload, ByteBuf buffer) throws Exception {
        buffer.writeInt(payload.getBrokerId());
    }

    @Override
    public int type() {
        return NsrCommandType.GET_REPLICA_BY_BROKER;
    }
}
