package io.chubao.joyqueue.nsr.network.codec;

import io.chubao.joyqueue.domain.DataCenter;
import io.chubao.joyqueue.network.serializer.Serializer;
import io.chubao.joyqueue.network.transport.command.Header;
import io.chubao.joyqueue.network.transport.command.Type;
import io.chubao.joyqueue.nsr.network.NsrPayloadCodec;
import io.chubao.joyqueue.nsr.network.command.GetDataCenterAck;
import io.chubao.joyqueue.nsr.network.command.NsrCommandType;
import io.netty.buffer.ByteBuf;

/**
 * @author wylixiaobin
 * Date: 2019/1/29
 */
public class GetDataCenterAckCodec implements NsrPayloadCodec<GetDataCenterAck>, Type {
    @Override
    public GetDataCenterAck decode(Header header, ByteBuf buffer) throws Exception {
        DataCenter dataCenter = new DataCenter();
        dataCenter.setCode(Serializer.readString(buffer));
        dataCenter.setName(Serializer.readString(buffer));
        dataCenter.setRegion(Serializer.readString(buffer));
        dataCenter.setUrl(Serializer.readString(buffer));
        return new GetDataCenterAck().dataCenter(dataCenter);
    }

    @Override
    public void encode(GetDataCenterAck payload, ByteBuf buffer) throws Exception {
        DataCenter dataCenter = payload.getDataCenter();
        Serializer.write(dataCenter.getCode(),buffer);
        Serializer.write(dataCenter.getName(),buffer);
        Serializer.write(dataCenter.getRegion(),buffer);
        Serializer.write(dataCenter.getUrl(),buffer);
    }

    @Override
    public int type() {
        return NsrCommandType.GET_DATACENTER_ACK;
    }
}
