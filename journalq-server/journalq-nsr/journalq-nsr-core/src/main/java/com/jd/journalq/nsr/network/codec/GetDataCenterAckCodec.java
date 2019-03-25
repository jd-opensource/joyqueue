package com.jd.journalq.nsr.network.codec;

import com.jd.journalq.common.domain.DataCenter;
import com.jd.journalq.common.network.serializer.Serializer;
import com.jd.journalq.common.network.transport.command.Header;
import com.jd.journalq.common.network.transport.command.Type;
import com.jd.journalq.nsr.network.NsrPayloadCodec;
import com.jd.journalq.nsr.network.command.GetDataCenterAck;
import com.jd.journalq.nsr.network.command.NsrCommandType;
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
